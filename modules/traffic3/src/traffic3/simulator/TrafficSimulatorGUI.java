package traffic3.simulator;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.SwingUtilities;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import traffic3.objects.TrafficArea;
import traffic3.objects.TrafficAgent;
//import traffic3.objects.TrafficBlockade;
import traffic3.manager.TrafficManager;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.gui.PanZoomListener;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.Edge;

/**
   A GUI for watching the traffic simulator.
*/
public class TrafficSimulatorGUI extends JPanel {
    private static final Color SELECTED_AREA_COLOUR = new Color(0, 0, 255, 128);
    private static final Color AREA_OUTLINE_COLOUR = new Color(0, 0, 0);
    private static final Color BLOCKADE_OUTLINE_COLOUR = new Color(0, 0, 0);
    private static final Color BLOCKADE_FILL_COLOUR = new Color(255, 0, 0, 128);

    private static final Stroke PASSABLE_EDGE_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private static final Stroke IMPASSABLE_EDGE_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private static final Stroke SELECTED_AREA_OUTLINE_STROKE = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private static final Stroke BLOCKADE_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    private static final int PATH_NODE_SIZE = 5;
    private static final int PATH_SPECIAL_NODE_SIZE = 9;
    private static final int TICK_TIME_MS = 10;

    private TrafficManager manager;

    private volatile boolean waitOnRefresh;
    private final Object lock = new Object();
    private CountDownLatch latch;

    private WorldView view;
    private JButton cont;
    private JCheckBox wait;
    private JCheckBox animate;
    private Timer timer;

    /**
       Construct a TrafficSimulatorGUI.
       @param manager The traffic manager.
    */
    public TrafficSimulatorGUI(TrafficManager manager) {
        super(new BorderLayout());
        this.manager = manager;
        waitOnRefresh = false;

        view = new WorldView();
        cont = new JButton("Continue");
        wait = new JCheckBox("Wait on refresh", waitOnRefresh);
        animate = new JCheckBox("Animate", false);
        cont.setEnabled(false);
        cont.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    synchronized (lock) {
                        if (latch != null) {
                            latch.countDown();
                        }
                    }
                    cont.setEnabled(false);
                }
            });
        wait.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    waitOnRefresh = wait.isSelected();
                }
            });
        animate.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (animate.isSelected()) {
                        timer.start();
                    }
                    else {
                        timer.stop();
                    }
                    cont.setEnabled(false);
                }
            });

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(wait, BorderLayout.WEST);
        buttons.add(cont, BorderLayout.CENTER);
        buttons.add(animate, BorderLayout.EAST);

        add(view, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        timer = new Timer(TICK_TIME_MS, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    synchronized (lock) {
                        if (latch != null) {
                            latch.countDown();
                        }
                    }
                }
            });
    }

    /**
       Initialise the GUI.
    */
    public void initialise() {
        view.initialise();
    }

    /**
       Refresh the view and wait for user input if required.
       @see #setWaitOnRefresh(boolean).
    */
    public void refresh() {
        repaint();
        if (waitOnRefresh) {
            SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!timer.isRunning()) {
                            cont.setEnabled(true);
                        }
                    }
                });
            synchronized (lock) {
                latch = new CountDownLatch(1);
            }
            try {
                latch.await();
            }
            catch (InterruptedException e) {
                Logger.error("Error waiting for continue", e);
            }
        }
    }

    /**
       Set whether to wait for the user before returning from a call to {@link #refresh()}.
       @param b Whether to wait on future calls to refresh.
    */
    public void setWaitOnRefresh(boolean b) {
        waitOnRefresh = b;
    }

    private class WorldView extends JComponent {
        private ScreenTransform transform;
        private TrafficArea selectedArea;
        private TrafficAgent selectedAgent;
        private Map<Shape, TrafficArea> areas;
        private Map<Shape, TrafficAgent> agents;

        public WorldView() {
        }

        public void initialise() {
            Rectangle2D bounds = null;
            for (TrafficArea area : manager.getAreas()) {
                Rectangle2D r = area.getArea().getShape().getBounds2D();
                if (bounds == null) {
                    bounds = new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
                }
                else {
                    Rectangle2D.union(bounds, r, bounds);
                }
            }
            transform = new ScreenTransform(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());
            new PanZoomListener(this).setScreenTransform(transform);
            selectedArea = null;
            areas = new HashMap<Shape, TrafficArea>();
            agents = new HashMap<Shape, TrafficAgent>();
            addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Point p = e.getPoint();
                        selectedArea = null;
                        selectedAgent = null;
                        for (Map.Entry<Shape, TrafficArea> next : areas.entrySet()) {
                            if (next.getKey().contains(p)) {
                                selectedArea = next.getValue();
                            }
                        }
                        for (Map.Entry<Shape, TrafficAgent> next : agents.entrySet()) {
                            if (next.getKey().contains(p)) {
                                selectedAgent = next.getValue();
                            }
                        }
                        repaint();
                    }
                });
        }

        @Override
        public void paintComponent(Graphics g) {
            int width = getWidth();
            int height = getHeight();
            Insets insets = getInsets();
            width -= insets.left + insets.right;
            height -= insets.top + insets.bottom;
            transform.rescale(width, height);
            Graphics2D copy = (Graphics2D)g.create(insets.left, insets.top, width, height);
            drawObjects(copy);
        }

        private void drawObjects(Graphics2D g) {
            drawAreas((Graphics2D)g.create());
            drawAgents((Graphics2D)g.create());
            drawBlockades((Graphics2D)g.create());
        }

        private void drawAreas(Graphics2D g) {
            areas.clear();
            for (TrafficArea area : manager.getAreas()) {
                Path2D shape = new Path2D.Double();
                List<Edge> edges = area.getArea().getEdges();
                Edge e = edges.get(0);
                shape.moveTo(transform.xToScreen(e.getStartX()), transform.yToScreen(e.getStartY()));
                for (Edge edge : edges) {
                    shape.lineTo(transform.xToScreen(edge.getEndX()), transform.yToScreen(edge.getEndY()));
                }
                if (area == selectedArea) {
                    g.setColor(SELECTED_AREA_COLOUR);
                    g.fill(shape);
                    g.setColor(AREA_OUTLINE_COLOUR);
                    paintEdges(edges, g);
                }
                else {
                    g.setColor(AREA_OUTLINE_COLOUR);
                    paintEdges(edges, g);
                }
                areas.put(shape, area);
            }
        }

        private void paintEdges(List<Edge> edges, Graphics2D g) {
            for (Edge edge : edges) {
                if (edge.isPassable()) {
                    g.setStroke(PASSABLE_EDGE_STROKE);
                }
                else {
                    g.setStroke(IMPASSABLE_EDGE_STROKE);
                }
                Line2D line = edge.getLine();
                g.drawLine(transform.xToScreen(line.getOrigin().getX()),
                           transform.yToScreen(line.getOrigin().getY()),
                           transform.xToScreen(line.getEndPoint().getX()),
                           transform.yToScreen(line.getEndPoint().getY()));
            }
        }

        private void drawBlockades(Graphics2D g) {
            /*
            for (TrafficBlockade b : manager.getBlockadeList()) {
                Path2D shape = b.getShape();
                g.setColor(BLOCKADE_FILL_COLOUR);
                g.fill(shape);
                g.setStroke(BLOCKADE_STROKE);
                g.setColor(BLOCKADE_OUTLINE_COLOUR);
                g.draw(shape);
            }
            */
        }

        private void drawAgents(Graphics2D g) {
            for (TrafficAgent agent : manager.getAgents()) {
                double agentX = agent.getX();
                double agentY = agent.getY();
                double ellipseX1 = agentX - agent.getRadius();
                double ellipseY1 = agentY + agent.getRadius();
                double ellipseX2 = agentX + agent.getRadius();
                double ellipseY2 = agentY - agent.getRadius();
                double velocityX = agentX + (agent.getVX() * 1000);
                double velocityY = agentY + (agent.getVX() * 1000);
                double forceX = agentX + (agent.getFX() * 1000);
                double forceY = agentY + (agent.getFX() * 1000);

                int x = transform.xToScreen(agentX);
                int y = transform.yToScreen(agentY);
                int x1 = transform.xToScreen(ellipseX1);
                int y1 = transform.yToScreen(ellipseY1);
                int x2 = transform.xToScreen(ellipseX2);
                int y2 = transform.yToScreen(ellipseY2);
                int vx = transform.xToScreen(velocityX);
                int vy = transform.yToScreen(velocityY);
                int fx = transform.xToScreen(forceX);
                int fy = transform.yToScreen(forceY);
                int ellipseWidth = x2 - x1;
                int ellipseHeight = y2 - y1;

                g.setColor(agent == selectedAgent ? Color.orange : Color.red);
                g.fillOval(x1, y1, ellipseWidth, ellipseHeight);
                g.setColor(Color.blue);
                g.drawLine(x, y, vx, vy);
                g.setColor(Color.green);
                g.drawLine(x, y, fx, fy);

                Shape shape = new Ellipse2D.Double(x1, y1, ellipseWidth, ellipseHeight);
                agents.put(shape, agent);

                // Draw the path of the selected agent
                if (agent == selectedAgent) {
                    List<Point2D> path = selectedAgent.getPath();
                    if (path != null) {
                        Point2D goal = selectedAgent.getFinalDestination();
                        Point2D current = selectedAgent.getCurrentDestination();
                        g.setColor(Color.gray);
                        int lastX = x;
                        int lastY = y;
                        for (Point2D next : path) {
                            int nodeX = transform.xToScreen(next.getX());
                            int nodeY = transform.yToScreen(next.getY());
                            g.fillOval(nodeX - (PATH_NODE_SIZE / 2), nodeY - (PATH_NODE_SIZE / 2), PATH_NODE_SIZE, PATH_NODE_SIZE);
                            g.drawLine(lastX, lastY, nodeX, nodeY);
                            lastX = nodeX;
                            lastY = nodeY;
                        }
                        if (current != null) {
                            g.setColor(Color.YELLOW);
                            int nodeX = transform.xToScreen(current.getX());
                            int nodeY = transform.yToScreen(current.getY());
                            g.fillOval(nodeX - (PATH_SPECIAL_NODE_SIZE / 2), nodeY - (PATH_SPECIAL_NODE_SIZE / 2), PATH_SPECIAL_NODE_SIZE, PATH_SPECIAL_NODE_SIZE);
                        }
                        if (goal != null) {
                            g.setColor(Color.WHITE);
                            int nodeX = transform.xToScreen(goal.getX());
                            int nodeY = transform.yToScreen(goal.getY());
                            g.fillOval(nodeX - (PATH_SPECIAL_NODE_SIZE / 2), nodeY - (PATH_SPECIAL_NODE_SIZE / 2), PATH_SPECIAL_NODE_SIZE, PATH_SPECIAL_NODE_SIZE);
                        }
                    }
                }
            }
        }
    }
}
