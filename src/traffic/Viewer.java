package traffic;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.util.*;
import traffic.object.*;
import rescuecore.RescueConstants;

public class Viewer extends JFrame implements Constants {
  private final JTextField waitTextField = new JTextField(4);
  private final JLabel     timeLabel     = new JLabel();
  private int waitingTime = VIEWER_WAITING_TIME;
  private boolean isWaiting = false;
  private Image m_motionlessObjectsImage;
  private HashSet m_laneSet = new HashSet();

  public Viewer() {
    JPanel menuPanel = new JPanel(new FlowLayout());

    final JButton startButton = new JButton("Stop");
    menuPanel.add(startButton);
    startButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	isWaiting = startButton.getText().equals("Start");
	startButton.setText(isWaiting ? "Stop" : "Start");
        isWaiting = !isWaiting; }});

    menuPanel.add(timeLabel);
    setTime();

    menuPanel.add(waitTextField);
    waitTextField.setText(String.valueOf(waitingTime));
    waitTextField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { waitingTime = Integer.parseInt(waitTextField.getText()); } });

    PaintField paintField = new PaintField();

    JPanel whole = new JPanel(new BorderLayout());
    whole.add(menuPanel,  BorderLayout.NORTH);
    whole.add(paintField, BorderLayout.CENTER);

    getContentPane().add(whole);
    pack();
    setSize(VIEWER_WIDTH, VIEWER_HEIGHT);
    setVisible(true);
  }

  public void setTime() {
    int time = WORLD.time();
    double sec = WORLD.sec();
    timeLabel.setText("   Time: " + time + ":" + sec + "   ");
  }

  public void waitProgress() {
    if (WORLD.time() <=  3)
      return;
    do {
      try { Thread.sleep(waitingTime); }
      catch (InterruptedException e) { e.printStackTrace(); System.exit(1); }
    } while (isWaiting);
  }

  private class PaintField extends JComponent {
    private int xbase;
    private int ybase;
    private double ratio;

    private int x(int x) { return               (int) (x * ratio + xbase); }
    private int y(int y) { return getHeight() - (int) (y * ratio + ybase); }

    public void paint(Graphics g) {
      if (WORLD.time() <= INITIALIZING_TIME)
	return;
      if (m_motionlessObjectsImage == null  || setBaseAndRatio()) {
	m_motionlessObjectsImage = createImage(getWidth(), getHeight());
	Graphics mg = m_motionlessObjectsImage.getGraphics();
	mg.setColor(new Color(140, 140, 140));
	mg.fillRect(0, 0, getWidth(), getHeight());
	
	Iterator it = WORLD.idObjMap.values().iterator();
	while (it.hasNext()) {
	  Object obj = it.next();
	  if      (obj instanceof Building) paintBuilding(mg, (Building) obj);
	  else if (obj instanceof Road)     paintRoad(mg, (Road) obj);
	}
      }
      g.drawImage(m_motionlessObjectsImage, 0, 0, this);

      for (int i = WORLD.movingObjectArray().length - 1;  i >= 0;  i --)
	paintMovingObject(g, WORLD.movingObjectArray()[i]);

      Iterator it = m_laneSet.iterator();
      while (it.hasNext()) {
	Lane lane = (Lane) it.next();
	if (lane.isBlocked())
	  paintBlock(g, lane);
	else
	  if (WORLD.time() >= BLOCKADE_INITIALIZING_TIME) it.remove();
      }
    }

    private boolean setBaseAndRatio() {
      double ratiox = (getWidth()  - VIEWER_GAP) / (double) (WORLD.maxX() - WORLD.minX());
      double ratioy = (getHeight() - VIEWER_GAP) / (double) (WORLD.maxY() - WORLD.minY());
      double preRatio = ratio;
      ratio = Math.min(ratiox, ratioy);
      xbase = (int) (VIEWER_GAP / 2 - WORLD.minX() * ratio);
      ybase = (int) (VIEWER_GAP / 2 - WORLD.minY() * ratio);
      return ratio != preRatio;
    }

    private void paintBuilding(Graphics g, Building bldg) {
      g.setColor(Color.gray.darker());
      Iterator it = bldg.entrances().iterator();
      while (it.hasNext()) {
	MotionlessObject mv = (MotionlessObject) it.next();
	g.drawLine(x(bldg.x()), y(bldg.y()), x(mv.x()), y(mv.y()));
      }
      g.setColor(Color.gray);
      g.drawRect(x(bldg.x()) - VIEWER_BUILDING_SIZE / 2, y(bldg.y()) - VIEWER_BUILDING_SIZE / 2, VIEWER_BUILDING_SIZE, VIEWER_BUILDING_SIZE);
    }

    private void paintRoad(Graphics g, Road road) {
      PointObject p1 = road.head(), p2 = road.tail();
      Lane l1 = (Lane) ((java.util.List) road.lanesTo(p1)).get(0);
      if (l1.dx < 0  ||  l1.dx == 0  &&  l1.dy < 0) { p1 = road.tail();  p2 = road.head(); }
      g.setColor(Color.white);
      paintLanes(g, road.lanesTo(p1));
      g.setColor(Color.red.darker());
      paintLanes(g, road.lanesTo(p2));
      g.setColor(Color.black);
      g.fillOval(x(p1.x()) - VIEWER_NODE_SIZE / 2, y(p1.y()) - VIEWER_NODE_SIZE / 2, VIEWER_NODE_SIZE, VIEWER_NODE_SIZE);
      g.fillOval(x(p2.x()) - VIEWER_NODE_SIZE / 2, y(p2.y()) - VIEWER_NODE_SIZE / 2, VIEWER_NODE_SIZE, VIEWER_NODE_SIZE);
    }

    private void paintLanes(Graphics g, java.util.List lanes) {
      Iterator it = lanes.iterator();
      while (it.hasNext()) {
	Lane lane = (Lane) it.next();
	paintLane(g, lane);
	m_laneSet.add(lane);
      }
    }

    private void paintLane(Graphics g, Lane lane) {
      PointObject f = lane.forward;
      PointObject b = lane.back;
      g.drawLine(x(b.x() + lane.dx), y(b.y() + lane.dy), x(f.x() + lane.dx), y(f.y() + lane.dy));
    }

    private void paintBlock(Graphics g, Lane lane) {
      int x = this.x(lane.road.x() + lane.dx) - VIEWER_BLOCK_SIZE / 2;
      int y = this.y(lane.road.y() + lane.dy) - VIEWER_BLOCK_SIZE / 2;
      g.setColor(Color.black);
      g.drawLine(x, y,                     x + VIEWER_BLOCK_SIZE, y + VIEWER_BLOCK_SIZE);
      g.drawLine(x, y + VIEWER_BLOCK_SIZE, x + VIEWER_BLOCK_SIZE, y);
      g.setColor(Color.cyan);
      g.drawLine(x + 1, y,                     x + VIEWER_BLOCK_SIZE + 1, y + VIEWER_BLOCK_SIZE);
      g.drawLine(x + 1, y + VIEWER_BLOCK_SIZE, x + VIEWER_BLOCK_SIZE + 1, y);
    }

    private void paintMovingObject(Graphics g, MovingObject mv) {
      Lane lane = mv.lane();
      int x = mv.x();  int y = mv.y();
      if (lane instanceof Lane) { x += lane.dx;  y += lane.dy; }
      x = this.x(x);  y = this.y(y);
      Color color;
      switch (mv.type()) {
      default:                   color = Color.black;   break;
      case RescueConstants.TYPE_CIVILIAN:        color = Color.green;   break;
      case RescueConstants.TYPE_AMBULANCE_TEAM:  color = Color.white;   break;
      case RescueConstants.TYPE_FIRE_BRIGADE:    color = Color.red;    break;
      case RescueConstants.TYPE_POLICE_FORCE:    color = Color.blue;  break;
      }
      g.setColor(color);
      g.fillOval(x - VIEWER_HUMANOID_SIZE / 2, y - VIEWER_HUMANOID_SIZE / 2, VIEWER_HUMANOID_SIZE, VIEWER_HUMANOID_SIZE);
      g.drawString("" + mv.id, x + VIEWER_HUMANOID_SIZE, y + VIEWER_HUMANOID_SIZE);
      g.setColor(color.darker());
      g.drawOval(x - VIEWER_HUMANOID_SIZE / 2, y - VIEWER_HUMANOID_SIZE / 2, VIEWER_HUMANOID_SIZE, VIEWER_HUMANOID_SIZE);
      // paintRoutePlan(g, mv);
    }

    private void paintRoutePlan(Graphics g, MovingObject mv) {
      g.setColor(Color.green);
      for (int i = 0;  i < mv.routePlan().size();  i ++) {
	MotionlessObject ml = mv.routePlan().get(i);
	if (ml instanceof Road) {
	  PointObject h = ((Road) ml).head();
	  PointObject t = ((Road) ml).tail();
	  g.drawLine(x(h.x()), y(h.y()), x(t.x()), y(t.y()));
	} else if (ml instanceof Node) {
	  g.drawOval(x(ml.x()) - VIEWER_NODE_SIZE, y(ml.y()) - VIEWER_NODE_SIZE, VIEWER_NODE_SIZE * 2, VIEWER_NODE_SIZE * 2);
	}
      }
      if (mv.lane() instanceof Lane) {
	MotionlessObject fwd = mv.lane().forward;
	g.setColor(Color.red);
	g.drawOval(x(fwd.x()) - VIEWER_NODE_SIZE, y(fwd.y()) - VIEWER_NODE_SIZE, VIEWER_NODE_SIZE * 2, VIEWER_NODE_SIZE * 2);
      }
    }
  }
}
