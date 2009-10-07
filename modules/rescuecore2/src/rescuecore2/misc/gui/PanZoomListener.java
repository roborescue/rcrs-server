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
        mouseDownX = transform.screenToX(e.getPoint().x);
        mouseDownY = transform.screenToY(e.getPoint().y);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (transform == null) {
            return;
        }
        transform.setFixedPoint(mouseDownX, mouseDownY, e.getPoint().x, e.getPoint().y);
        component.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (transform == null) {
            return;
        }
        if (e.getWheelRotation() < 0) {
            Point p = e.getPoint();
            Insets insets = component.getInsets();
            double x = transform.screenToX(p.x - insets.left);
            double y = transform.screenToY(p.y - insets.top);
            transform.setFixedPoint(x, y, p.x - insets.left, p.y - insets.top);
            transform.zoomIn();
            component.repaint();
        }
        if (e.getWheelRotation() > 0) {
            transform.zoomOut();
            component.repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}
}
