package rescuecore2.misc.gui;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.Point;
import java.awt.Insets;
import javax.swing.JComponent;

/**
   A mouse listener that will handle panning and zooming in conjunction with a ScreenTransform.
 */
public class PanZoomListener implements MouseListener, MouseMotionListener, MouseWheelListener {
    private static final int DEFAULT_MOUSE_ZOOM_THRESHOLD = 100;
    private double mouseDownX;
    private double mouseDownY;
    private int zoomMouseDownY;
    private boolean dragging;
    private boolean zooming;
    private ScreenTransform transform;
    private JComponent component;
    private boolean enabled;
    private int panTriggerModifiers;
    private int zoomTriggerModifiers;
    private int zoomThreshold;

    /**
       Construct a PanZoomListener that listens for events on a JComponent.
       @param component The component to listen for mouse events on.
    */
    public PanZoomListener(JComponent component) {
        this.component = component;
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
        component.addMouseWheelListener(this);
        panTriggerModifiers = InputEvent.BUTTON1_DOWN_MASK;
        zoomTriggerModifiers = InputEvent.BUTTON2_DOWN_MASK;
        zoomThreshold = DEFAULT_MOUSE_ZOOM_THRESHOLD;
        enabled = true;
    }

    /**
       Set the screen transform.
       @param t The screen transform.
     */
    public void setScreenTransform(ScreenTransform t) {
        this.transform = t;
    }

    /**
       Enable or disable this PanZoomListener.
       @param b Whether to process events or not.
    */
    public void setEnabled(boolean b) {
        enabled = b;
        if (!enabled) {
            dragging = false;
            zooming = false;
        }
    }

    /**
       Set the modifiers that will trigger zooming.
       @param modifiers The modifiers mask that must be set for zooming to begin.
    */
    public void setZoomTriggerModifiers(int modifiers) {
        zoomTriggerModifiers = modifiers;
        zooming = false;
    }

    /**
    Set the modifiers that will trigger panning.
    @param modifiers The modifiers mask that must be set for panning to begin.
     */
    public void setPanTriggerModifiers(int modifiers) {
        panTriggerModifiers = modifiers;
        dragging = false;
    }

    /**
       Set the pan trigger modifiers so that pressing the left mouse button triggers panning.
    */
    public void setPanOnLeftMouse() {
        setPanTriggerModifiers(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
       Set the pan trigger modifiers so that pressing the right mouse button triggers panning.
    */
    public void setPanOnRightMouse() {
        setPanTriggerModifiers(InputEvent.BUTTON3_DOWN_MASK);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!enabled) {
            return;
        }
        if (transform == null) {
            return;
        }
        if ((e.getModifiersEx() & panTriggerModifiers) == panTriggerModifiers) {
            Point p = fixEventPoint(e.getPoint());
            mouseDownX = transform.screenToX(p.x);
            mouseDownY = transform.screenToY(p.y);
            dragging = true;
        }
        if ((e.getModifiersEx() & zoomTriggerModifiers) == zoomTriggerModifiers) {
            zoomMouseDownY = e.getPoint().y;
            zooming = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!enabled) {
            return;
        }
        if ((e.getModifiersEx() & panTriggerModifiers) != panTriggerModifiers) {
            dragging = false;
        }
        if ((e.getModifiersEx() & zoomTriggerModifiers) != zoomTriggerModifiers) {
            zooming = false;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!enabled) {
            return;
        }
        if (transform == null) {
            return;
        }
        if (dragging) {
            Point p = fixEventPoint(e.getPoint());
            transform.makeCentreRelativeTo(mouseDownX, mouseDownY, p.x, p.y);
            component.repaint();
        }
        if (zooming) {
            int newY = e.getPoint().y;
            if (newY < zoomMouseDownY - zoomThreshold) {
                transform.zoomIn();
                zoomMouseDownY = newY;
                component.repaint();
            }
            else if (newY > zoomMouseDownY + zoomThreshold) {
                transform.zoomOut();
                zoomMouseDownY = newY;
                component.repaint();
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!enabled) {
            return;
        }
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
