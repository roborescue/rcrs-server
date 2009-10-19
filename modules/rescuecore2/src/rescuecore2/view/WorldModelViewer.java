package rescuecore2.view;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.Command;
import rescuecore2.config.Config;
import rescuecore2.misc.gui.PanZoomListener;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A JComponent that shows a view of a world model.
 */
public abstract class WorldModelViewer extends JComponent {
    private static final Color BACKGROUND = new Color(0x1B898D);

    /** The world model. */
    protected WorldModel<? extends Entity> world;

    /** The current commands. */
    protected Collection<Command> commands;

    /** The updates. */
    protected Collection<Entity> updates;

    private PanZoomListener panZoom;
    private ScreenTransform transform;

    private List<RenderedObject> objects;
    private Set<ViewListener> listeners;

    /**
       Construct a new WorldModelViewer.
     */
    public WorldModelViewer() {
        objects = new ArrayList<RenderedObject>();
        listeners = new HashSet<ViewListener>();
        addMouseListener(new ViewerMouseListener());
        panZoom = new PanZoomListener(this);
        setBackground(BACKGROUND);
        commands = new HashSet<Command>();
        updates = new HashSet<Entity>();
    }

    /**
       Initialise this viewer. The default implementation does nothing.
       @param config The system configuration.
    */
    public void initialise(Config config) {
    }

    /**
       Get the name of this viewer. Default implementation just calls toString.
       @return The name of this viewer.
    */
    public String getViewerName() {
        return toString();
    }

    /**
       Disable the pan-zoom feature.
    */
    public void disablePanZoom() {
        removeMouseListener(panZoom);
        removeMouseMotionListener(panZoom);
        removeMouseWheelListener(panZoom);
        transform.resetZoom();
        repaint();
    }

    /**
       Enable the pan-zoom feature.
     */
    public void enablePanZoom() {
        addMouseListener(panZoom);
        addMouseMotionListener(panZoom);
        addMouseWheelListener(panZoom);
    }

    /**
       Add a view listener.
       @param l The listener to add.
     */
    public void addViewListener(ViewListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    /**
       Remove a view listener.
       @param l The listener to remove.
     */
    public void removeViewListener(ViewListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
       View a world model, command list and update list.
       @param newModel The new world model.
       @param newCommands The new commands list.
       @param newUpdates The new updates list.
     */
    public void view(WorldModel<? extends Entity> newModel, Collection<Command> newCommands, Collection<Entity> newUpdates) {
        this.world = newModel;
        commands.clear();
        if (newCommands != null) {
            commands.addAll(newCommands);
        }
        updates.clear();
        if (newUpdates != null) {
            updates.addAll(newUpdates);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        Insets insets = getInsets();
        width -= insets.left + insets.right;
        height -= insets.top + insets.bottom;
        objects.clear();
        transform.rescale(width, height);
        Graphics2D copy = (Graphics2D)g.create(insets.left, insets.top, width, height);
        objects.addAll(render(copy, transform, width, height));
    }

    /**
       Render the world and return the set of RenderedObjects.
       @param graphics The graphics to draw on. The origin is guaranteed to be at 0, 0.
       @param transform The ScreenTransform that will convert world coordinates to screen coordinates.
       @param width The width of the viewport.
       @param height The height of the viewport.
       @return The set of RenderedObjects.
     */
    // CHECKSTYLE:OFF:HiddenField Supress bogus warning about transform hiding a field: it's not really hidden since this is an abstract method and transform is private.
    protected abstract Collection<RenderedObject> render(Graphics2D graphics, ScreenTransform transform, int width, int height);
    // CHECKSTYLE:ON:HiddenField

    /**
       Update the bounds of the viewed model.
       @param xMin The minimum x coordinate.
       @param yMin The minimum y coordinate.
       @param xMax The maximum x coordinate.
       @param yMax The maximum y coordinate.
     */
    protected void updateBounds(double xMin, double yMin, double xMax, double yMax) {
        transform = new ScreenTransform(xMin, yMin, xMax, yMax);
        panZoom.setScreenTransform(transform);
    }

    private List<RenderedObject> getObjectsAtPoint(int x, int y) {
        List<RenderedObject> result = new ArrayList<RenderedObject>();
        for (RenderedObject next : objects) {
            if (next.getShape().contains(x, y)) {
                result.add(next);
            }
        }
        return result;
    }

    private void fireObjectsClicked(List<RenderedObject> clicked) {
        List<ViewListener> all = new ArrayList<ViewListener>();
        synchronized (listeners) {
            all.addAll(listeners);
        }
        for (ViewListener next : all) {
            next.objectsClicked(this, clicked);
        }
    }

    private class ViewerMouseListener implements MouseListener {
        @Override
        public void mousePressed(MouseEvent e) {}
        @Override
        public void mouseReleased(MouseEvent e) {}
        @Override
        public void mouseClicked(MouseEvent e) {
            Point p = e.getPoint();
            Insets insets = getInsets();
            List<RenderedObject> clicked = getObjectsAtPoint(p.x - insets.left, p.y - insets.top);
            fireObjectsClicked(clicked);
        }
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e) {}
    }
}