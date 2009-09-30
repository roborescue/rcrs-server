package rescuecore2.misc.gui;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.BasicStroke;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CyclicBarrier;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;

public class ShapeDebugFrame extends JFrame {
    private JButton step;
    private JButton cont;
    private ShapeViewer viewer;
    private ShapeInfoKey key;
    private CyclicBarrier barrier;
    private boolean enabled;
    private Collection<ShapeInfo> background;

    public ShapeDebugFrame() {
        barrier = new CyclicBarrier(2);
        viewer = new ShapeViewer();
        key = new ShapeInfoKey();
        step = new JButton("Step");
        cont = new JButton("Continue");
        add(viewer, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new GridLayout(1, 2));
        buttons.add(step);
        buttons.add(cont);
        add(buttons, BorderLayout.SOUTH);
        add(key, BorderLayout.EAST);
        key.setBorder(BorderFactory.createTitledBorder("Key"));
        viewer.setBorder(BorderFactory.createTitledBorder("Shapes"));
        step.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        //                        setVisible(false);
                        barrier.await();
                    }
                    catch (Exception ex) {}
                }
            });
        cont.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        //                        setVisible(false);
                        disable();
                        barrier.await();
                    }
                    catch (Exception ex) {}
                }
            });
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    try {
                        barrier.await();
                    }
                    catch (Exception ex) {}
                }
            });
        enabled = true;
        pack();
    }

    public void setBackground(Collection<ShapeInfo> back) {
        background = back;
    }

    public void clearBackground() {
        background = new ArrayList<ShapeInfo>();
    }

    public void show(Collection<? extends ShapeInfo>... shapes) {
        List<ShapeInfo> all = new ArrayList<ShapeInfo>();
        for (Collection<? extends ShapeInfo> next : shapes) {
            all.addAll(next);
        }
        ShapeInfo[] s = all.toArray(new ShapeInfo[0]);
        show(s);
    }

    public void show(Collection<? extends ShapeInfo> shapes) {
        ShapeInfo[] s = shapes.toArray(new ShapeInfo[0]);
        show(s);
    }

    public void show(final ShapeInfo... shapes) {
        if (!enabled) {
            return;
        }
        final ShapeInfo[] allShapes = new ShapeInfo[shapes.length + background.size()];
        background.toArray(allShapes);
        System.arraycopy(shapes, 0, allShapes, background.size(), shapes.length);
        setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    key.setShapes(allShapes);
                    viewer.setShapes(allShapes);
                    repaint();
                }
            });
        try {
            barrier.await();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
        setVisible(false);
    }

    private static class ShapeViewer extends JComponent {
        private ShapeInfo[] shapes;
        private ScreenTransform transform;
        private PanZoomListener panZoom;

        public ShapeViewer() {
            panZoom = new PanZoomListener(this);
            addMouseListener(panZoom);
            addMouseMotionListener(panZoom);
            addMouseWheelListener(panZoom);
        }

        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (shapes == null || shapes.length == 0) {
                return;
            }
            Insets insets = getInsets();
            int width = getWidth() - insets.left - insets.right;
            int height = getHeight() - insets.top - insets.bottom;
            transform.rescale(width, height);
            for (ShapeInfo next : shapes) {
                Graphics g = graphics.create(insets.left, insets.top, width, height);
                next.paint((Graphics2D)g, transform);
            }
        }

        public Dimension getPreferredSize() {
            return new Dimension(500, 500);
        }

        public void setShapes(ShapeInfo... s) {
            shapes = s;
            double minX = Double.POSITIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            for (ShapeInfo next : shapes) {
                Rectangle2D bounds = next.getBounds();
                if (bounds != null) {
                    minX = Math.min(minX, bounds.getMinX());
                    maxX = Math.max(maxX, bounds.getMaxX());
                    minY = Math.min(minY, bounds.getMinY());
                    maxY = Math.max(maxY, bounds.getMaxY());
                }
            }
            transform = new ScreenTransform(minX, minY, maxX, maxY);
            panZoom.setScreenTransform(transform);
            repaint();
        }
    }

    private static class ShapeInfoKey extends JComponent {
        private ShapeInfo[] shapes;

        public Dimension getPreferredSize() {
            return new Dimension(500, 500);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (shapes == null || shapes.length ==0) {
                return;
            }
            FontMetrics metrics = g.getFontMetrics();
            int height = metrics.getHeight();
            int y = getInsets().top;
            int x = getInsets().left + 5;
            for (ShapeInfo next : shapes) {
                if (next.getName() == null || "".equals(next.getName())) {
                    continue;
                }
                g.setColor(next.getColor());
                if (next.isFilled()) {
                    g.fillRect(x, y + (height / 2) - 2, 50, 5);
                }
                else {
                    g.drawRect(x, y + (height / 2) - 2, 50, 5);
                }
                g.setColor(Color.black);
                g.drawString(next.getName(), x + 55, y + metrics.getAscent());
                y += height + 5;
            }
        }

        public void setShapes(ShapeInfo[] s) {
            this.shapes = s;
            repaint();
        }
    }

    public abstract static class ShapeInfo {
        protected Color color;
        protected String name;
        protected boolean fill;
        protected Rectangle2D bounds;

        protected ShapeInfo(String name,Color color, boolean fill, Rectangle2D bounds)  {
            this.name = name;
            this.color = color;
            this.fill = fill;
            this.bounds = bounds;
        }

        public abstract void paint(Graphics2D g, ScreenTransform transform);

        public Color getColor() {
            return color;
        }

        public String getName() {
            return name;
        }

        public boolean isFilled() {
            return fill;
        }

        public Rectangle2D getBounds() {
            return bounds;
        }
    }

    public static class AWTShapeInfo extends ShapeInfo {
        private Shape shape;

        public AWTShapeInfo(Shape shape, String name, Color color, boolean fill) {
            super(name, color, fill, shape.getBounds2D());
            this.shape = shape;
        }

        @Override
        public void paint(Graphics2D g, ScreenTransform transform) {
            if (shape instanceof Area && ((Area)shape).isEmpty()) {
                return;
            }
            Path2D path = new Path2D.Double();
            PathIterator pi = shape.getPathIterator(null);
            double[] d = new double[6];
            while (!pi.isDone()) {
                switch (pi.currentSegment(d)) {
                case PathIterator.SEG_MOVETO:
                    path.moveTo(transform.xToScreen(d[0]), transform.yToScreen(d[1]));
                    break;
                case PathIterator.SEG_LINETO:
                    path.lineTo(transform.xToScreen(d[0]), transform.yToScreen(d[1]));
                    break;
                case PathIterator.SEG_CLOSE:
                    path.closePath();
                    break;
                case PathIterator.SEG_QUADTO:
                    path.quadTo(transform.xToScreen(d[0]), transform.yToScreen(d[1]), transform.xToScreen(d[2]), transform.yToScreen(d[3]));
                    break;
                case PathIterator.SEG_CUBICTO:
                    path.curveTo(transform.xToScreen(d[0]), transform.yToScreen(d[1]), transform.xToScreen(d[2]), transform.yToScreen(d[3]), transform.xToScreen(d[4]), transform.yToScreen(d[5]));
                    break;
                }
                pi.next();
            }
            g.setColor(color);
            if (fill) {
                g.fill(path);
            }
            else {
                g.draw(path);
            }
        }
    }

    public static class Point2DShapeInfo extends ShapeInfo {
        private static final int SIZE = 3;

        private Point2D point;

        public Point2DShapeInfo(Point2D point, String name, Color color, boolean fill) {
            super(name, color, fill, new Rectangle2D.Double(point.getX(), point.getY(), 0, 0));
            this.point = point;
        }

        @Override
        public void paint(Graphics2D g, ScreenTransform transform) {
            int x = transform.xToScreen(point.getX());
            int y = transform.yToScreen(point.getY());
            g.setColor(color);
            if (fill) {
                g.fillRect(x - SIZE, y - SIZE, SIZE * 2, SIZE * 2);
            }
            else {
                g.drawLine(x - SIZE, y - SIZE, x + SIZE, y + SIZE);
                g.drawLine(x - SIZE, y + SIZE, x + SIZE, y - SIZE);
            }
        }
    }

    public static class Line2DShapeInfo extends ShapeInfo {
        private Line2D line;
        private final static int SIZE = 1;
        private final static BasicStroke FILL_STROKE = new BasicStroke(SIZE * 3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        private final static BasicStroke EMPTY_STROKE = new BasicStroke(SIZE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

        public Line2DShapeInfo(Line2D line, String name, Color color, boolean fill) {
            super(name, color, fill, line == null ? new Rectangle2D.Double(0, 0, 0, 0) : new Rectangle2D.Double(line.getOrigin().getX(), line.getOrigin().getY(), line.getDirection().getX(), line.getDirection().getY()));
            this.line = line;
        }

        @Override
        public void paint(Graphics2D g, ScreenTransform transform) {
            if (line == null) {
                return;
            }
            Point2D start = line.getOrigin();
            Point2D end = line.getEndPoint();
            int x1 = transform.xToScreen(start.getX());
            int y1 = transform.yToScreen(start.getY());
            int x2 = transform.xToScreen(end.getX());
            int y2 = transform.yToScreen(end.getY());
            if (fill) {
                g.setStroke(FILL_STROKE);
            }
            else {
                g.setStroke(EMPTY_STROKE);
            }
            g.setColor(color);
            g.drawLine(x1, y1, x2, y2);
        }
    }
}