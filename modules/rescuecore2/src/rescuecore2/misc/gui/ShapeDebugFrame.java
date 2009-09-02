package rescuecore2.misc.gui;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CyclicBarrier;

public class ShapeDebugFrame extends JFrame {
    private JButton step;
    private ShapeViewer viewer;
    private ShapeInfoKey key;
    private CyclicBarrier barrier;

    public ShapeDebugFrame() {
        barrier = new CyclicBarrier(2);
        viewer = new ShapeViewer();
        key = new ShapeInfoKey();
        step = new JButton("Step");
        add(viewer, BorderLayout.CENTER);
        add(step, BorderLayout.SOUTH);
        add(key, BorderLayout.EAST);
        key.setBorder(BorderFactory.createTitledBorder("Key"));
        viewer.setBorder(BorderFactory.createTitledBorder("Shapes"));
        step.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        setVisible(false);
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
        pack();
    }

    public void show(final ShapeInfo... shapes) {
        setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    key.setShapes(shapes);
                    viewer.setShapes(shapes);
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
            Graphics g = graphics.create(insets.left, insets.top, width, height);
            transform.rescale(width, height);
            for (ShapeInfo next : shapes) {
                Path2D path = new Path2D.Double();
                if (next.shape instanceof Area && ((Area)next.shape).isEmpty()) {
                    continue;
                }
                PathIterator pi = next.shape.getPathIterator(null);
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
                g.setColor(next.color);
                if (next.fill) {
                    ((Graphics2D)g).fill(path);
                }
                else {
                    ((Graphics2D)g).draw(path);
                }
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
                if (next.shape instanceof Area && ((Area)next.shape).isEmpty()) {
                    continue;
                }
                Rectangle2D bounds = next.shape.getBounds2D();
                minX = Math.min(minX, bounds.getMinX());
                maxX = Math.max(maxX, bounds.getMaxX());
                minY = Math.min(minY, bounds.getMinY());
                maxY = Math.max(maxY, bounds.getMaxY());
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
                g.setColor(next.color);
                if (next.fill) {
                    g.fillRect(x, y + (height / 2) - 2, 50, 5);
                }
                else {
                    g.drawRect(x, y + (height / 2) - 2, 50, 5);
                }
                g.setColor(Color.black);
                g.drawString(next.name, x + 55, y + metrics.getAscent());
                y += height + 5;
            }
        }

        public void setShapes(ShapeInfo[] s) {
            this.shapes = s;
            repaint();
        }
    }

    public static class ShapeInfo {
        Shape shape;
        String name;
        Color color;
        boolean fill;

        public ShapeInfo(Shape shape, String name, Color color, boolean fill) {
            this.shape = shape;
            this.name = name;
            this.color = color;
            this.fill = fill;
        }
    }
}