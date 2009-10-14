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
import java.util.concurrent.BrokenBarrierException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;

/**
   A JFrame that can be used to debug geometric shape operations. When {@link #enable enabled} this frame will block whenever a {@link #show(ShapeInfo...)} method is called until the user clicks on a button to continue. The "step" button will cause the show method to return and leave the frame visible and activated. The "continue" button will hide and {@link #deactivate} the frame so that further calls to show will return immediately.
 */
public class ShapeDebugFrame extends JFrame {
    private static final int DISPLAY_WIDTH = 500;
    private static final int DISPLAY_HEIGHT = 500;
    private static final int LEGEND_WIDTH = 500;
    private static final int LEGEND_HEIGHT = 500;

    private JButton step;
    private JButton cont;
    private ShapeViewer viewer;
    private ShapeInfoLegend legend;
    private CyclicBarrier barrier;
    private boolean enabled;
    private Collection<ShapeInfo> background;

    /**
       Construct a new ShapeDebugFrame.
     */
    public ShapeDebugFrame() {
        barrier = new CyclicBarrier(2);
        viewer = new ShapeViewer();
        legend = new ShapeInfoLegend();
        step = new JButton("Step");
        cont = new JButton("Continue");
        add(viewer, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new GridLayout(1, 2));
        buttons.add(step);
        buttons.add(cont);
        add(buttons, BorderLayout.SOUTH);
        add(legend, BorderLayout.EAST);
        legend.setBorder(BorderFactory.createTitledBorder("Legend"));
        viewer.setBorder(BorderFactory.createTitledBorder("Shapes"));
        step.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        //                        setVisible(false);
                        barrier.await();
                    }
                    catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    catch (BrokenBarrierException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        cont.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        //                        setVisible(false);
                        deactivate();
                        barrier.await();
                    }
                    catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    catch (BrokenBarrierException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    try {
                        barrier.await();
                    }
                    catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    catch (BrokenBarrierException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        enabled = true;
        pack();
    }

    /**
       Set the "background" shapes. These will be drawn on every invocation of show.
       @param back The new background shapes. This should not be null.
     */
    public void setBackground(Collection<ShapeInfo> back) {
        background = back;
    }

    /**
       Clear the "background" shapes.
     */
    public void clearBackground() {
        background = new ArrayList<ShapeInfo>();
    }

    /**
       Show a set of ShapeInfo objects. If this frame is enabled then this method will block until the user clicks a button to continue.
       @param shapes A list of collections of ShapeInfo objects.
     */
    public void show(Collection<? extends ShapeInfo>... shapes) {
        List<ShapeInfo> all = new ArrayList<ShapeInfo>();
        for (Collection<? extends ShapeInfo> next : shapes) {
            all.addAll(next);
        }
        ShapeInfo[] s = all.toArray(new ShapeInfo[0]);
        show(s);
    }

    /**
       Show a set of ShapeInfo objects. If this frame is enabled then this method will block until the user clicks a button to continue.
       @param shapes A Collection of ShapeInfo objects.
     */
    public void show(Collection<? extends ShapeInfo> shapes) {
        ShapeInfo[] s = shapes.toArray(new ShapeInfo[0]);
        show(s);
    }

    /**
       Show a set of ShapeInfo objects. If this frame is enabled then this method will block until the user clicks a button to continue.
       @param shapes A list of ShapeInfo objects.
     */
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
                    legend.setShapes(allShapes);
                    viewer.setShapes(allShapes);
                    repaint();
                }
            });
        try {
            barrier.await();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    /**
       Activate this frame. Future calls to show will block until the user clicks a button.
     */
    public void activate() {
        enabled = true;
    }

    /**
       Deactivate and hides this frame. Future calls to show will return immediately.
     */
    public void deactivate() {
        enabled = false;
        setVisible(false);
    }

    private static class ShapeViewer extends JComponent {
        private ShapeInfo[] shapes;
        private ScreenTransform transform;
        private PanZoomListener panZoom;

        /**
           Create a ShapeViewer.
        */
        public ShapeViewer() {
            panZoom = new PanZoomListener(this);
        }

        @Override
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

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT);
        }

        /**
           Set the list of ShapeInfo objects to draw.
           @param s The new list of ShapeInfo objects.
         */
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

    /**
       The legend for the debug frame.
     */
    private static class ShapeInfoLegend extends JComponent {
        private static final int ROW_OFFSET = 5;
        private static final int X_INDENT = 5;
        private static final int BAR_WIDTH = 50;
        private static final int BAR_HEIGHT = 5;

        private ShapeInfo[] shapes;

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(LEGEND_WIDTH, LEGEND_HEIGHT);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (shapes == null || shapes.length == 0) {
                return;
            }
            FontMetrics metrics = g.getFontMetrics();
            int height = metrics.getHeight();
            int y = getInsets().top;
            int x = getInsets().left + X_INDENT;
            for (ShapeInfo next : shapes) {
                if (next.getName() == null || "".equals(next.getName())) {
                    continue;
                }
                g.setColor(next.getColour());
                if (next.isFilled()) {
                    g.fillRect(x, y + (height / 2) - (BAR_HEIGHT / 2), BAR_WIDTH, BAR_HEIGHT);
                }
                else {
                    g.drawRect(x, y + (height / 2) - (BAR_HEIGHT / 2), BAR_WIDTH, BAR_HEIGHT);
                }
                g.setColor(Color.black);
                g.drawString(next.getName(), x + BAR_WIDTH + X_INDENT, y + metrics.getAscent());
                y += height + ROW_OFFSET;
            }
        }

        /**
           Set the list of shapes.
           @param s The new list of shapes.
         */
        public void setShapes(ShapeInfo[] s) {
            this.shapes = s;
            repaint();
        }
    }

    /**
       This class captures information about a shape that should be displayed on-screen.
     */
    public abstract static class ShapeInfo {
        /** The colour of the shape. */
        protected Color colour;
        /** The name of the shape. */
        protected String name;
        /** Whether to fill the shape. */
        protected boolean fill;
        /** The bounds of the shape. */
        protected Rectangle2D bounds;

        /**
           Construct a new ShapeInfo object.
           @param name The name of the shape.
           @param colour The colour of the shape.
           @param fill Whether to fill the shape.
           @param bounds The bounding box of the shape.
         */
        protected ShapeInfo(String name, Color colour, boolean fill, Rectangle2D bounds)  {
            this.name = name;
            this.colour = colour;
            this.fill = fill;
            this.bounds = bounds;
        }

        /**
           Paint this ShapeInfo on a Graphics2D object.
           @param g The Graphics2D to draw on.
           @param transform The current screen transform.
         */
        public abstract void paint(Graphics2D g, ScreenTransform transform);

        /**
           Get the colour of this shape info.
           @return The colour.
         */
        public Color getColour() {
            return colour;
        }

        /**
           Get the name of this shape info.
           @return The name.
         */
        public String getName() {
            return name;
        }

        /**
           Get whether this shape info should be filled or not.
           @return Whether to fill the shape or not.
         */
        public boolean isFilled() {
            return fill;
        }

        /**
           Get the bounding box of the shape.
           @return The bounding box.
         */
        public Rectangle2D getBounds() {
            return bounds;
        }
    }

    /**
       A ShapeInfo that encapsulates an awt Shape.
     */
    public static class AWTShapeInfo extends ShapeInfo {
        private Shape shape;

        /**
           Construct a new AWTShapeInfo object.
           @param shape The shape to display.
           @param name The name of the shape.
           @param colour The colour of the shape.
           @param fill Whether to fill the shape.
         */
        public AWTShapeInfo(Shape shape, String name, Color colour, boolean fill) {
            super(name, colour, fill, shape.getBounds2D());
            this.shape = shape;
        }

        @Override
        public void paint(Graphics2D g, ScreenTransform transform) {
            if (shape instanceof Area && ((Area)shape).isEmpty()) {
                return;
            }
            Path2D path = new Path2D.Double();
            PathIterator pi = shape.getPathIterator(null);
            // CHECKSTYLE:OFF:MagicNumber
            double[] d = new double[6];
            while (!pi.isDone()) {
                int type = pi.currentSegment(d);
                switch (type) {
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
                default:
                    throw new RuntimeException("Unexpected PathIterator constant: " + type);
                }
                pi.next();
            }
            // CHECKSTYLE:ON:MagicNumber
            g.setColor(colour);
            if (fill) {
                g.fill(path);
            }
            else {
                g.draw(path);
            }
        }
    }

    /**
       A ShapeInfo that encapsulates a Point2D.
     */
    public static class Point2DShapeInfo extends ShapeInfo {
        private static final int SIZE = 3;

        private Point2D point;

        /**
           Construct a new Point2DShapeInfo object.
           @param point The point to display.
           @param name The name of the point.
           @param colour The colour of the point.
           @param fill Whether to "fill" the point. If true then the point will be drawn as a rectangle; if false it will be drawn as a cross.
         */
        public Point2DShapeInfo(Point2D point, String name, Color colour, boolean fill) {
            super(name, colour, fill, new Rectangle2D.Double(point.getX(), point.getY(), 0, 0));
            this.point = point;
        }

        @Override
        public void paint(Graphics2D g, ScreenTransform transform) {
            int x = transform.xToScreen(point.getX());
            int y = transform.yToScreen(point.getY());
            g.setColor(colour);
            if (fill) {
                g.fillRect(x - SIZE, y - SIZE, SIZE * 2, SIZE * 2);
            }
            else {
                g.drawLine(x - SIZE, y - SIZE, x + SIZE, y + SIZE);
                g.drawLine(x - SIZE, y + SIZE, x + SIZE, y - SIZE);
            }
        }
    }

    /**
       A ShapeInfo that encapsulates a Line2D.
     */
    public static class Line2DShapeInfo extends ShapeInfo {
        private static final int SIZE = 1;
        private static final BasicStroke FILL_STROKE = new BasicStroke(SIZE * 3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        private static final BasicStroke EMPTY_STROKE = new BasicStroke(SIZE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

        private Line2D line;

        /**
           Construct a new Line2DShapeInfo object.
           @param line The line to display.
           @param name The name of the line.
           @param colour The colour of the line.
           @param fill Whether to "fill" the line: if true then the line will be drawn with a wide stroke.
         */
        public Line2DShapeInfo(Line2D line, String name, Color colour, boolean fill) {
            super(name, colour, fill, line == null ? new Rectangle2D.Double(0, 0, 0, 0) : new Rectangle2D.Double(line.getOrigin().getX(), line.getOrigin().getY(), line.getDirection().getX(), line.getDirection().getY()));
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
            g.setColor(colour);
            g.drawLine(x1, y1, x2, y2);
        }
    }
}