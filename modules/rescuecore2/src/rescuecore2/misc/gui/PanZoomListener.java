package rescuecore2.misc.gui;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.Point;
import java.awt.Insets;
import javax.swing.JComponent;

/**
   A mouse listener that will handle panning and zooming in conjunction with a ScreenTransform.
 */
public class PanZoomListener implements MouseListener, MouseMotionListener, MouseWheelListener {
    private double mouseDownX;
    private double mouseDownY;
    private boolean dragging;
    private ScreenTransform transform;
    private JComponent component;

    /**
       Construct a PanZoomListener that listens for events on a JComponent.
       @param component The component to listen for mouse events on.
    */
    public PanZoomListener(JComponent component) {
        this.component = component;
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
        component.addMouseWheelListener(this);
    }

    /**
       Set the screen transform.
       @param t The screen transform.
     */
    public void setScreenTransform(ScreenTransform t) {
        this.transform = t;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (transform == null) {
            return;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            Point p = fixEventPoint(e.getPoint());
            mouseDownX = transform.screenToX(p.x);
            mouseDownY = transform.screenToY(p.y);
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            dragging = false;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (transform == null || !dragging) {
            return;
        }
        Point p = fixEventPoint(e.getPoint());
        transform.makeCentreRelativeTo(mouseDownX, mouseDownY, p.x, p.y);
        component.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (transform == null) {
            return;
        }
        if (e.getWheelRotation() < 0) {
            Point p = fixEventPoint(e.getPoint());
            double x = transform.screenToX(p.x);
            double y = transform.screenToY(p.y);
            transform.zoomIn();
            transform.makeCentreRelativeTo(x, y, p.x, p.y);
            component.repaint();
        }
        if (e.getWheelRotation() > 0) {
            Point p = fixEventPoint(e.getPoint());
            double x = transform.screenToX(p.x);
            double y = transform.screenToY(p.y);
            transform.zoomOut();
            transform.makeCentreRelativeTo(x, y, p.x, p.y);
            component.repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    private Point fixEventPoint(Point p) {
        Insets insets = component.getInsets();
        return new Point(p.x - insets.left, p.y - insets.top);
    }
}
