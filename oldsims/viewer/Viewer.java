package viewer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import viewer.object.*;
import rescuecore.RescueConstants;
import rescuecore.InputBuffer;

public class Viewer extends JFrame implements Constants {
    private boolean m_doAnimation = VIEWER_DO_ANIMATION_AS_A_DEFAULT;
    public boolean doAnimation() { return m_doAnimation; }
    private int m_intervalEachCycle = VIEWER_DEFAULT_INTERVAL_EACH_CYCLE;

    private Image m_motionlessObjectsImage;
    private boolean m_wasChangedBackground;
    private JLabel m_statusLabel = new JLabel();

    public Viewer() {
        Container p0 = getContentPane();
        p0.setLayout(new BorderLayout());
        p0.add(controlPanel(), BorderLayout.SOUTH);
        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(statusPanel(),    BorderLayout.NORTH);
        p1.add(new PaintField(), BorderLayout.CENTER);
        p0.add(p1, BorderLayout.CENTER);
        pack();
        //setSize(VIEWER_WIDTH, VIEWER_HEIGHT);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("RobocupRescue");
    }

    private JPanel controlPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(VIEWER_WIDTH, 20));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        addIntervalEachCycleSlider(panel);
        addDoAnimateCheckBox(panel);
        return panel;
    }

    private void addIntervalEachCycleSlider(JPanel panel) {
        panel.add(new JLabel("Interval "));
        final JSlider intervalEachCycleSlider
        = new JSlider(JSlider.HORIZONTAL, 0, 5000, VIEWER_DEFAULT_INTERVAL_EACH_CYCLE);
        intervalEachCycleSlider.setPreferredSize(new Dimension(100, 20));
        intervalEachCycleSlider.setMaximumSize(new Dimension(100, 20));

        setIntervalSilderTipText(intervalEachCycleSlider);
        ToolTipManager tipMan = ToolTipManager.sharedInstance();
        tipMan.setInitialDelay(100);
        tipMan.setDismissDelay(2000);

        panel.add(intervalEachCycleSlider);
        intervalEachCycleSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    m_intervalEachCycle = ((JSlider) e.getSource()).getValue();
                    setIntervalSilderTipText(intervalEachCycleSlider);
                }});
    }

    private void setIntervalSilderTipText(JSlider slider) {
        slider.setToolTipText(slider.getValue() + " [m sec/cycle]");
    }


    private void addDoAnimateCheckBox(JPanel panel) {
        final JCheckBox doAnimateCheckBox = new JCheckBox("Animation", true);
        panel.add(doAnimateCheckBox);
        doAnimateCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    m_doAnimation = !m_doAnimation;
                }});
    }

    private JPanel statusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(Color.white);
        m_statusLabel.setFont(m_statusLabel.getFont().deriveFont(Font.PLAIN, 20));
        setStatus();
        panel.add(m_statusLabel);
        return panel;
    }

    public void setStatus() {
        StringBuffer status = new StringBuffer();

        String name = Main.teamName();
        if (name instanceof String)
            status.append("Team: ").append(name);

        status.append("        Time: ").append(WORLD.time());

        DecimalFormat df = new DecimalFormat();
        df.setMaximumIntegerDigits(3);
        df.setMaximumFractionDigits(6);
        df.setMinimumFractionDigits(6);
        status.append("        Score: ").append(df.format(WORLD.score()));

        m_statusLabel.setText(status.toString());
    }

    private class PaintField extends JComponent {
        private int m_xbase;
        private int m_ybase;
        private double m_ratio;

        public void paint(Graphics g) {
            paintBackground(g);
            paintFires(g);
            paintActions(g);
            paintMovingObjects(g);
            paintBlockades(g);
        }

        private int m_lastPaintBackground = 0;

        private void paintBackground(Graphics g) {
            setBaseAndRatio();
            if (m_motionlessObjectsImage == null
                || m_wasChangedBackground
                || m_lastPaintBackground <= 3  &&  m_lastPaintBackground != WORLD.time()) {
                m_wasChangedBackground = false;
                m_lastPaintBackground = WORLD.time();
                m_motionlessObjectsImage = createImage(getWidth(), getHeight());
                Graphics mg = m_motionlessObjectsImage.getGraphics();
                mg.setColor(new Color(140, 140, 140));
                mg.fillRect(0, 0, getWidth(), getHeight());

                Iterator it = WORLD.buildingList.iterator();
                while (it.hasNext())
                    paintBuildingWithState(mg, (Building) it.next());
                it = WORLD.roadList.iterator();
                while (it.hasNext()) {
                    mg.setColor(new Color(185, 185, 185));
                    paintRoad(mg, (Road) it.next());
                }
            }
            g.drawImage(m_motionlessObjectsImage, 0, 0, this);
        }

        private void setBaseAndRatio() {
            double preRatio = m_ratio;
            int dw = WORLD.maxX() - WORLD.minX();
            int dh = WORLD.maxY() - WORLD.minY();
            m_ratio = Math.min((getWidth()  - VIEWER_GAP) / (double) dw,
                               (getHeight() - VIEWER_GAP) / (double) dh);
            m_xbase = (int) ((getWidth()  - dw * m_ratio) / 2 - WORLD.minX() * m_ratio);
            m_ybase = (int) ((getHeight() - dh * m_ratio) / 2 - WORLD.minY() * m_ratio);
            m_wasChangedBackground |= m_ratio != preRatio;
        }

        private int x(int x) {
            int result = (int) (x * m_ratio + m_xbase);
            if (result < 0) new RuntimeException().printStackTrace();
            return result;
	}
        private int y(int y) { return getHeight() - (int) (y * m_ratio + m_ybase); }

        private void paintBuilding(Graphics g, Building bldg) {
            paintColoredShape(g, shape(bldg));
        }

        private void paintColoredShape(Graphics g, Polygon shape) {
            g.fillPolygon(shape);
            g.setColor(g.getColor().darker().darker());
            g.drawPolygon(shape);
        }

        private Polygon shape(Building bldg) {
            Polygon shape = new Polygon();
            int[] apexes = bldg.buildingApexes();
            //	  System.out.println("Finding shape of building");
            //	  System.out.println("Center at "+bldg.x()+", "+bldg.y());
            for (int i = 0;  i < apexes.length;  i += 2) {
                shape.addPoint(x(apexes[i]), y(apexes[i + 1]));
		//		System.out.println("Apex at "+apexes[i]+", "+apexes[i+1]);
            }
            return shape;
        }

        private void paintBuildingWithState(Graphics g, Building bldg) {
            g.setColor(new Color(120, 120, 120));
            Iterator it = bldg.entrances().iterator();
            while (it.hasNext()) {
                MotionlessObject mv = (MotionlessObject) it.next();
                g.drawLine(x(bldg.x()), y(bldg.y()), x(mv.x()), y(mv.y()));
            }

            Polygon shape = shape(bldg);
            paintBroken(g, bldg, shape);

            g.setColor(Color.gray.darker());
            g.drawPolygon(shape);

            paintBuildingType(g, bldg, shape);
        }

        private void paintBroken(Graphics g, Building bldg, Polygon shape) {
            if (bldg.brokenness() == 0  ||  !bldg.isNoBurned())
                return;
            int b = Math.max(0, 135 - bldg.brokenness() / 4);
            g.setColor(new Color(b, b, b));
            paintColoredShape(g, shape);
        }

        private void paintBuildingType(Graphics g, Building bldg, Polygon shape) {
            if (bldg.type() == RescueConstants.TYPE_BUILDING)
                return;
            if (bldg.type() == RescueConstants.TYPE_REFUGE) g.setColor(new Color(80, 180, 80));
            else                            g.setColor(new Color(200, 200, 200));
            paintColoredShape(g, shape);
        }

        private void paintFires(Graphics g) {
            Iterator it = WORLD.buildingList.iterator();
            while (it.hasNext())
                paintFire(g, (Building) it.next());
        }

        private void paintFire(Graphics g, Building bldg) {
            if (bldg.isNoBurned())
                return;
            Color c = ViewerColors.getBuildingColorForViewer(bldg.fieryness());
            g.setColor(c);  

            //     switch (bldg.fieryness()) {
            //		case 1 :
            //			g.setColor(new Color(176, 176, 56));
            //			break;
            //		case 2 :
            //			g.setColor(new Color(184, 122, 50));
            //			break;
            //		case 3 :
            //			g.setColor(new Color(140, 52, 52));
            //			break;
            //			//case 1: g.setColor(new Color(157, 119, 119)); break;
            //			//case 2: g.setColor(new Color(175,  98,  98)); break;
            //			//case 3: g.setColor(new Color(197,  70,  70)); break;
            //		case 4 :
            //			g.setColor(new Color(100, 140, 210));
            //			break;
            //		case 5 :
            //			g.setColor(new Color(120, 140, 180));
            //			break;
            //		case 6 :
            //			g.setColor(new Color(130, 140, 150));
            //			break;
            //		case 7 :
            //			g.setColor(new Color(100, 90, 80));
            //			break;
            //        default: Util.myassert(false);
            //      }
            paintBuilding(g, bldg);
        }

        private void paintRoad(Graphics g, Road road) {
            PointObject h = road.head();
            PointObject t = road.tail();
            g.fillPolygon(shape(road));
            g.setColor(Color.gray.darker());
            paintNode(g, h, 0);
            paintNode(g, t, 0);
        }

        private Polygon shape(Road road) {
            PointObject h = road.head();
            PointObject t = road.tail();
            int width = Math.min(VIEWER_ROAD_MAX_WIDTH, road.width());
            int dx = (h.y() - t.y()) * width / road.length() / 2;
            int dy = (h.x() - t.x()) * width / road.length() / 2;
            Polygon shape = new Polygon();
            shape.addPoint(x(h.x() - dx), y(h.y() + dy));
            shape.addPoint(x(h.x() + dx), y(h.y() - dy));
            shape.addPoint(x(t.x() + dx), y(t.y() - dy));
            shape.addPoint(x(t.x() - dx), y(t.y() + dy));
            return shape;
        }

        private void paintNode(Graphics g, PointObject po, int dsize) {
            int size = VIEWER_NODE_SIZE + dsize;
            g.fillOval(x(po.x()) - size / 2, y(po.y()) - size / 2, size, size);
        }

        private void paintBlockades(Graphics g) {
            Iterator it = WORLD.roadList.iterator();
            while (it.hasNext())
                paintBlockade(g, (Road) it.next());
        }

        private void paintBlockade(Graphics g, Road road) {
            if (road.blockedLines() == 0)
                return;
            final int B = VIEWER_BLOCKADE_SIZE;
            int x = x(road.x()) - B / 2;
            int y = y(road.y()) - B / 2;
            g.setColor(road.aliveLinesToHead() > 0  ||  road.aliveLinesToTail() > 0
                       ? Color.gray.darker() : Color.black);
            g.drawLine(x, y,     x + B, y + B);
            g.drawLine(x, y + B, x + B, y    );
            x ++;
            g.drawLine(x, y,     x + B, y + B);
            g.drawLine(x, y + B, x + B, y    );
        }

        private void paintMovingObjects(Graphics g) {
            paintMovingObjects(g, WORLD.civilianList);
            paintMovingObjects(g, WORLD.policeForceList);
            paintMovingObjects(g, WORLD.fireBrigadeList);
            paintMovingObjects(g, WORLD.ambulanceTeamList);
            paintLoadedMvObjs(g);
        }

        private void paintMovingObjects(Graphics g, Collection objs) {
            Iterator it = objs.iterator();
            while (it.hasNext()) {
                MovingObject mv = (MovingObject) it.next();
                if (mv.position() instanceof MotionlessObject)
                    paintColoredMovingObject(g, (MovingObject) mv, 0);
            }
        }

        private void paintLoadedMvObjs(Graphics g) {
            Iterator it = WORLD.movingObjectList.iterator();
            while (it.hasNext()) {
                MovingObject mv = (MovingObject) it.next();
                if (mv.position() instanceof MotionlessObject)
                    continue;
                paintColoredMovingObject(g, (MovingObject) mv.position(), +2);
                paintColoredMovingObject(g, mv,                           -2);
            }
        }

        private void paintColoredMovingObject(Graphics g, MovingObject mv, int dsize) {
            Color color;
            switch (mv.type()) {
            case RescueConstants.TYPE_CIVILIAN:        color = Color.green;  break;
            case RescueConstants.TYPE_AMBULANCE_TEAM:  color = Color.white;  break;
            case RescueConstants.TYPE_FIRE_BRIGADE:    color = Color.red;    break;
            case RescueConstants.TYPE_POLICE_FORCE:    color = Color.blue; break;
            default: Util.myassert(false); throw new Error();
            }
            int hp = ((Humanoid) mv).hp();
            if (hp == 0)
                color = Color.black;
            else
                for (int i = 10000 - hp;  i > 0;  i -= 3500) // 10000: max hp
                    color = color.darker();
            g.setColor(color);

            paintMovingObject(g, mv, dsize);
        }

        private void paintMovingObject(Graphics g, MovingObject mv, int dsize) {
            int x = x(mv.x());
            int y = y(mv.y());
            int h = paintingSizeOfMovingObject(mv) + dsize;
            g.fillOval(x - h / 2, y - h / 2, h, h);
            g.setColor(g.getColor().darker().darker().darker());
            g.drawOval(x - h / 2, y - h / 2, h, h);
        }

        private int paintingSizeOfMovingObject(MovingObject mv) {
            return VIEWER_HUMANOID_SIZE - ((mv instanceof Civilian) ? 1 : 0);
        }

        private void paintObject(Graphics g, Object obj) {
            if      (obj instanceof Road) paintRoad(g, (Road) obj);
            else if (obj instanceof Node) paintNode(g, (Node) obj, +2);
            else if (obj instanceof Building) paintBuilding(g, (Building) obj);
            else if (obj instanceof MovingObject) paintMovingObject(g, (MovingObject) obj, +2);
            else Util.myassert(false);
        }

        private void paintActions(Graphics g) {
            for (Iterator it = WORLD.movingObjectList.iterator();  it.hasNext();  ) {
                MovingObject mv = (MovingObject) it.next();
                String action = mv.actionType();
                if ("AK_LOAD".equals(action)
                    || "AK_UNLOAD".equals(action)
                    || "AK_RESCUE".equals(action)) {
                    g.setColor(new Color(190, 190, 170));
                    paintMovingObject(g, mv, + 6);
                }
                if ("AK_EXTINGUISH".equals(action)) {
                    Set targets = new HashSet();
                    InputBuffer dis = mv.actionData();
                    while (true) {
                        int id = dis.readInt();
                        if (id == 0)
                            break;
                        targets.add(WORLD.get(id));
                        dis.skip(4 * 4);  // sizeof(int) * {direction, x, y, quantity}
                    }
                    g.setColor(targets.size() == 1
                               ? Color.blue.brighter().brighter()
                               : Color.cyan);
                    for (Iterator it2 = targets.iterator();  it2.hasNext();  ) {
                        Building b = (Building) it2.next();
                        g.drawLine(x(mv.x()), y(mv.y()), x(b.x()), y(b.y()));
                    }
                }
                if ("AK_CLEAR".equals(action)) {
                    g.setColor(new Color(170, 170, 90));
                    paintMovingObject(g, mv, +6);
                }
            }
        }

        private Collection objectsAt(int x, int y) {
            Collection objs = new ArrayList();
            Iterator it = WORLD.idObjMap.values().iterator();
            while (it.hasNext()) {
                RealObject obj = (RealObject) it.next();
                if (obj instanceof Node  ||  obj instanceof MovingObject) {
                    int dx = x - x(obj.x());
                    int dy = y - y(obj.y());
                    int d = ((obj instanceof Node) ? VIEWER_NODE_SIZE : paintingSizeOfMovingObject((MovingObject) obj)) / 2 + 1;
                    if (dx * dx + dy * dy <= d * d)
                        objs.add(obj);
                } else if (obj instanceof Building) {
                    if (shape((Building) obj).contains(x, y))
                        objs.add(obj);
                } else if (obj instanceof Road) {
                    if (shape((Road) obj).contains(x, y))
                        objs.add(obj);
                }
            }
            return objs;
        }
    }

    public void animate() {
        WORLD.extractNextPositionPropertys();
        for (int i = numOfExposuresEachCycle();  i > 0  &&  m_doAnimation;  i --) {
            Iterator it = WORLD.movingObjectList.iterator();
            while (it.hasNext())
                ((MovingObject) it.next()).move();
            repaint();
            try { Thread.sleep(VIEWER_INTERVAL_EACH_EXPOSURE); }
            catch (InterruptedException e) { Util.myassert(false); }
        }
        return;
    }

    public int numOfExposuresEachCycle() { return m_intervalEachCycle / VIEWER_INTERVAL_EACH_EXPOSURE; }

    public void repaintWithWaiting() {
        repaint();
        try { Thread.sleep(m_intervalEachCycle); }
        catch (InterruptedException e) { Util.myassert(false); }
    }
}
