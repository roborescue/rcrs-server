package rescuecore2.misc.gui;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
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
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

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

    private JLabel title;
    private JButton step;
    private JButton cont;
    private ShapeViewer viewer;
    private ShapeInfoLegend legend;
    private CyclicBarrier barrier;
    private boolean enabled;
    private Collection<ShapeInfo> background;
    private boolean backgroundEnabled;

    /**
       Construct a new ShapeDebugFrame.
    */
    public ShapeDebugFrame() {
        barrier = new CyclicBarrier(2);
        viewer = new ShapeViewer();
        legend = new ShapeInfoLegend();
        step = new JButton("Step");
        cont = new JButton("Continue");
        title = new JLabel();
        add(title, BorderLayout.NORTH);
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
                        barrier.await();
                    }
                    // CHECKSTYLE:OFF:EmptyBlock
                    catch (InterruptedException ex) {
                        // Ignore
                    }
                    catch (BrokenBarrierException ex) {
                        // Ignore
                    }
                    // CHECKSTYLE:ON:EmptyBlock
                }
            });
        cont.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        deactivate();
                        barrier.await();
                    }
                    // CHECKSTYLE:OFF:EmptyBlock
                    catch (InterruptedException ex) {
                        // Ignore
                    }
                    catch (BrokenBarrierException ex) {
                        // Ignore
                    }
                    // CHECKSTYLE:ON:EmptyBlock
                }
            });
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    try {
                        barrier.await();
                    }
                    // CHECKSTYLE:OFF:EmptyBlock
                    catch (InterruptedException ex) {
                        // Ignore
                    }
                    catch (BrokenBarrierException ex) {
                        // Ignore
                    }
                    // CHECKSTYLE:ON:EmptyBlock
                }
            });
        enabled = true;
        backgroundEnabled = true;
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
       Set whether the background is drawn or not.
       @param b True if the background should be drawn, false otherwise.
    */
    public void setBackgroundEnabled(boolean b) {
        backgroundEnabled = b;
    }

    /**
       Show a set of ShapeInfo objects. If this frame is enabled then this method will block until the user clicks a button to continue.
       @param description A description.
       @param shapes A list of collections of ShapeInfo objects.
    */
    public void show(String description, Collection<? extends ShapeInfo>... shapes) {
        List<ShapeInfo> all = new ArrayList<ShapeInfo>();
        for (Collection<? extends ShapeInfo> next : shapes) {
            all.addAll(next);
        }
        show(description, all);
    }

    /**
       Show a set of ShapeInfo objects. If this frame is enabled then this method will block until the user clicks a button to continue.
       @param description A description.
       @param shapes An array of ShapeInfo objects.
    */
    public void show(String description, ShapeInfo... shapes) {
        show(description, Arrays.asList(shapes));
    }

    /**
       Show a set of ShapeInfo objects. If this frame is enabled then this method will block until the user clicks a button to continue.
       @param description A description.
       @param shapes A collection of ShapeInfo objects.
    */
    public void show(final String description, final Collection<ShapeInfo> shapes) {
        if (!enabled) {
            return;
        }
        final List<ShapeInfo> allShapes = new ArrayList<ShapeInfo>();
        if (backgroundEnabled && background != null) {
            allShapes.addAll(background);
        }
        allShapes.addAll(shapes);
        setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (description == null) {
                        title.setText("");
                    }
                    else {
                        title.setText(description);
                    }
                    legend.setShapes(allShapes);
                    viewer.setShapes(allShapes);
                    viewer.zoomTo(shapes);
                    repaint();
                }
            });
        try {
            barrier.await();
        }
        // CHECKSTYLE:OFF:EmptyBlock
        catch (InterruptedException e) {
            // Ignore
        }
        catch (BrokenBarrierException e) {
            // Ignore
        }
        // CHECKSTYLE:ON:EmptyBlock
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

    private static Rectangle2D getBounds(Collection<ShapeInfo> shapes) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (ShapeInfo next : shapes) {
            Shape bounds = next.getBoundsShape();
            if (bounds != null) {
                Rectangle2D rect = bounds.getBounds2D();
                minX = Math.min(minX, rect.getMinX());
                maxX = Math.max(maxX, rect.getMaxX());
                minY = Math.min(minY, rect.getMinY());
                maxY = Math.max(maxY, rect.getMaxY());
            }
            java.awt.geom.Point2D point = next.getBoundsPoint();
            if (point != null) {
                minX = Math.min(minX, point.getX());
                maxX = Math.max(maxX, point.getX());
                minY = Math.min(minY, point.getY());
                maxY = Math.max(maxY, point.getY());
            }
        }
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    private static class ShapeViewer extends JComponent {
        private List<ShapeInfo> shapes;
        private ScreenTransform transform;
        private PanZoomListener panZoom;
        private Map<Shape, ShapeInfo> drawnShapes;

        /**
           Create a ShapeViewer.
        */
        public ShapeViewer() {
            panZoom = new PanZoomListener(this);
            drawnShapes = new HashMap<Shape, ShapeInfo>();
            shapes = new ArrayList<ShapeInfo>();
            addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        Insets insets = getInsets();
                        Point p = new Point(e.getPoint());
                        p.translate(-insets.left, -insets.top);
                        List<ShapeInfo> shapes = getShapesAtPoint(p);
                        for (ShapeInfo next : shapes) {
                            System.out.println(next.getObject());
                        }
                    }
                });
        }

        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            drawnShapes.clear();
            if (shapes.isEmpty()) {
                return;
            }
            Insets insets = getInsets();
            int width = getWidth() - insets.left - insets.right;
            int height = getHeight() - insets.top - insets.bottom;
            transform.rescale(width, height);
            for (ShapeInfo next : shapes) {
                boolean visible = transform.isInView(next.getBoundsShape()) || transform.isInView(next.getBoundsPoint());
                if (visible) {
                    Graphics g = graphics.create(insets.left, insets.top, width, height);
                    Shape shape = next.paint((Graphics2D)g, transform);
                    if (shape != null) {
                        drawnShapes.put(shape, next);
                    }
                }
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
        public void setShapes(Collection<ShapeInfo> s) {
            shapes.clear();
            shapes.addAll(s);
            Rectangle2D bounds = ShapeDebugFrame.getBounds(shapes);
            transform = new ScreenTransform(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());
            panZoom.setScreenTransform(transform);
            repaint();
        }

        /**
           Zoom to show a set of ShapeInfo objects.
           @param zoom The set of objects to zoom to.
        */
        public void zoomTo(Collection<ShapeInfo> zoom) {
            Rectangle2D bounds = ShapeDebugFrame.getBounds(zoom);
            // Increase the bounds by 10%
            double newX = bounds.getMinX() - (bounds.getWidth() / 10);
            double newY = bounds.getMinY() - (bounds.getHeight() / 10);
            double newWidth = bounds.getWidth() * 1.2;
            double newHeight = bounds.getHeight() * 1.2;
            bounds.setRect(newX, newY, newWidth, newHeight);
            transform.show(bounds);
            repaint();
        }

        private List<ShapeInfo> getShapesAtPoint(Point p) {
            List<ShapeInfo> result = new ArrayList<ShapeInfo>();
            for (Map.Entry<Shape, ShapeInfo> next : drawnShapes.entrySet()) {
                Shape shape = next.getKey();
                if (shape.contains(p)) {
                    result.add(next.getValue());
                }
            }
            return result;
        }
    }

    /**
       The legend for the debug frame.
    */
    private static class ShapeInfoLegend extends JComponent {
        private static final int ROW_OFFSET = 5;
        private static final int X_INDENT = 5;
        private static final int ENTRY_WIDTH = 50;
        private static final int ENTRY_HEIGHT = 9;

        private List<ShapeInfo> shapes;

        ShapeInfoLegend() {
            shapes = new ArrayList<ShapeInfo>();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(LEGEND_WIDTH, LEGEND_HEIGHT);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (shapes.isEmpty()) {
                return;
            }
            Set<String> seen = new HashSet<String>();
            FontMetrics metrics = g.getFontMetrics();
            int height = metrics.getHeight();
            int y = getInsets().top;
            int x = getInsets().left + X_INDENT;
            for (ShapeInfo next : shapes) {
                String name = next.getName();
                if (name == null || "".equals(name)) {
                    continue;
                }
                if (seen.contains(name)) {
                    continue;
                }
                seen.add(name);
                next.paintLegend((Graphics2D)g.create(x, y + (height / 2) - (ENTRY_HEIGHT / 2), ENTRY_WIDTH, ENTRY_HEIGHT), ENTRY_WIDTH, ENTRY_HEIGHT);
                g.setColor(Color.black);
                g.drawString(next.getName(), x + ENTRY_WIDTH + X_INDENT, y + metrics.getAscent());
                y += height + ROW_OFFSET;
            }
        }

        /**
           Set the list of shapes.
           @param s The new list of shapes.
        */
        public void setShapes(Collection<ShapeInfo> s) {
            shapes.clear();
            shapes.addAll(s);
            repaint();
        }
    }

    /**
       This class captures information about a shape that should be displayed on-screen.
    */
    public abstract static class ShapeInfo {
        /** The name of the shape. */
        protected String name;
        /** The object this shape represents. */
        private Object object;

        /**
           Construct a new ShapeInfo object.
           @param object The object this shape represents.
           @param name The name of the shape.
        */
        protected ShapeInfo(Object object, String name)  {
            this.object = object;
            this.name = name;
        }

        /**
           Paint this ShapeInfo on a Graphics2D object.
           @param g The Graphics2D to draw on.
           @param transform The current screen transform.
           @return A shape for mouseover detection.
        */
        public abstract Shape paint(Graphics2D g, ScreenTransform transform);

        /**
           Paint this ShapeInfo on a the legend.
           @param g The Graphics2D to draw on.
           @param width The available width.
           @param width The available height.
        */
        public abstract void paintLegend(Graphics2D g, int width, int height);

        /**
           Get the object this shape represents.
           @return The object.
        */
        public Object getObject() {
            return object;
        }

        /**
           Get the name of this shape info.
           @return The name.
        */
        public String getName() {
            return name;
        }

        /**
           Get the bounding shape of this shape.
           @return The bounding shape or null if this shape represents a point.
        */
        public abstract Shape getBoundsShape();

        /**
           Get the point representing this shape.
           @return The shape point or null if this shape does not represent a point.
        */
        public abstract java.awt.geom.Point2D getBoundsPoint();
    }

    /**
       A ShapeInfo that encapsulates an awt Shape.
    */
    public static class AWTShapeInfo extends ShapeInfo {
        private Shape shape;
        private boolean fill;
        private Color colour;
        private Rectangle2D bounds;

        /**
           Construct a new AWTShapeInfo object.
           @param shape The shape to display.
           @param name The name of the shape.
           @param colour The colour of the shape.
           @param fill Whether to fill the shape.
        */
        public AWTShapeInfo(Shape shape, String name, Color colour, boolean fill) {
            super(shape, name);
            this.shape = shape;
            this.fill = fill;
            this.colour = colour;
            if (shape != null) {
                bounds = shape.getBounds2D();
            }
        }

        @Override
        public Shape paint(Graphics2D g, ScreenTransform transform) {
            if (shape == null || (shape instanceof Area && ((Area)shape).isEmpty())) {
                return null;
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
            return path.createTransformedShape(null);
        }

        @Override
        public void paintLegend(Graphics2D g, int width, int height) {
            if (shape == null) {
                return;
            }
            g.setColor(colour);
            if (fill) {
                g.fillRect(0, 0, width, height);
            }
            else {
                g.drawRect(0, 0, width - 1, height - 1);
            }
        }

        @Override
        public Rectangle2D getBoundsShape() {
            return bounds;
        }

        @Override
        public java.awt.geom.Point2D getBoundsPoint() {
            return null;
        }
    }

    /**
       A ShapeInfo that encapsulates a Point2D.
    */
    public static class Point2DShapeInfo extends ShapeInfo {
        private static final int SIZE = 3;

        private Point2D point;
        private java.awt.geom.Point2D boundsPoint;
        private boolean square;
        private Color colour;

        /**
           Construct a new Point2DShapeInfo object.
           @param point The point to display.
           @param name The name of the point.
           @param colour The colour of the point.
           @param square Whether to draw as a square or a cross. If false then a cross will be drawn.
        */
        public Point2DShapeInfo(Point2D point, String name, Color colour, boolean square) {
            super(point, name);
            this.point = point;
            this.square = square;
            this.colour = colour;
            if (point != null) {
                boundsPoint = new java.awt.geom.Point2D.Double(point.getX(), point.getY());
            }
        }

        @Override
        public Shape paint(Graphics2D g, ScreenTransform transform) {
            if (point == null) {
                return null;
            }
            int x = transform.xToScreen(point.getX());
            int y = transform.yToScreen(point.getY());
            g.setColor(colour);
            if (square) {
                g.fillRect(x - SIZE, y - SIZE, SIZE * 2, SIZE * 2);
            }
            else {
                g.drawLine(x - SIZE, y - SIZE, x + SIZE, y + SIZE);
                g.drawLine(x - SIZE, y + SIZE, x + SIZE, y - SIZE);
            }
            return new Rectangle(x - SIZE, y - SIZE, SIZE * 2, SIZE * 2);
        }

        @Override
        public void paintLegend(Graphics2D g, int width, int height) {
            if (point == null) {
                return;
            }
            g.setColor(colour);
            int x = (width / 2);
            int y = (height / 2);
            if (square) {
                g.fillRect(x - SIZE, y - SIZE, SIZE * 2, SIZE * 2);
            }
            else {
                g.drawLine(x - SIZE, y - SIZE, x + SIZE, y + SIZE);
                g.drawLine(x - SIZE, y + SIZE, x + SIZE, y - SIZE);
            }
        }

        @Override
        public Shape getBoundsShape() {
            return null;
        }

        @Override
        public java.awt.geom.Point2D getBoundsPoint() {
            return boundsPoint;
        }
    }

    /**
       A ShapeInfo that encapsulates a Line2D.
    */
    public static class Line2DShapeInfo extends ShapeInfo {
        private static final int SIZE = 1;
        private static final BasicStroke THICK_STROKE = new BasicStroke(SIZE * 3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        private static final BasicStroke THIN_STROKE = new BasicStroke(SIZE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

        private Line2D line;
        private java.awt.geom.Line2D bounds;
        private boolean arrow;
        private boolean thick;
        private Color colour;

        /**
           Construct a new Line2DShapeInfo object.
           @param line The line to display.
           @param name The name of the line.
           @param colour The colour of the line.
           @param thick Whether to draw the line with a thick stroke.
           @param arrow Whether to draw an arrow showing the direction of the line.
        */
        public Line2DShapeInfo(Line2D line, String name, Color colour, boolean thick, boolean arrow) {
            super(line, name);
            this.line = line;
            this.arrow = arrow;
            this.thick = thick;
            this.colour = colour;
            if (line != null) {
                bounds = new java.awt.geom.Line2D.Double(line.getOrigin().getX(), line.getOrigin().getY(), line.getEndPoint().getX(), line.getEndPoint().getY());
            }
        }

        @Override
        public Shape paint(Graphics2D g, ScreenTransform transform) {
            if (line == null) {
                return null;
            }
            Point2D start = line.getOrigin();
            Point2D end = line.getEndPoint();
            int x1 = transform.xToScreen(start.getX());
            int y1 = transform.yToScreen(start.getY());
            int x2 = transform.xToScreen(end.getX());
            int y2 = transform.yToScreen(end.getY());
            if (thick) {
                g.setStroke(THICK_STROKE);
            }
            else {
                g.setStroke(THIN_STROKE);
            }
            g.setColor(colour);
            g.drawLine(x1, y1, x2, y2);
            if (arrow) {
                DrawingTools.drawArrowHeads(x1, y1, x2, y2, g);
            }
            return g.getStroke().createStrokedShape(new java.awt.geom.Line2D.Double(x1, y1, x2, y2));
        }

        @Override
        public void paintLegend(Graphics2D g, int width, int height) {
            if (thick) {
                g.setStroke(THICK_STROKE);
            }
            else {
                g.setStroke(THIN_STROKE);
            }
            g.setColor(colour);
            g.drawLine(0, height / 2, width, height / 2);
            if (arrow) {
                DrawingTools.drawArrowHeads(0, height / 2, width, height / 2, g);
            }
        }

        @Override
        public java.awt.geom.Line2D getBoundsShape() {
            return bounds;
        }

        @Override
        public java.awt.geom.Point2D getBoundsPoint() {
            return null;
        }
    }
}