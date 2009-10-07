package rescuecore2.view;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

import rescuecore2.worldmodel.WorldModel;

/**
   A JComponent that shows a view of a world model.
   @param <T> The subclass of WorldModel that this viewer understands.
 */
public abstract class WorldModelViewer<T extends WorldModel> extends JComponent {
    private static final Color BACKGROUND = new Color(0x1B898D);

    /**
       The world model.
     */
    protected T world;

    private List<ViewLayer<? super T>> layers;
    private List<RenderedObject> objects;
    private List<ViewListener> listeners;

    /**
       Construct a new WorldModelViewer.
     */
    public WorldModelViewer() {
        layers = new ArrayList<ViewLayer<? super T>>();
        objects = new ArrayList<RenderedObject>();
        listeners = new ArrayList<ViewListener>();
        addMouseListener(new ViewerMouseListener());
        setBackground(BACKGROUND);
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
       Add a view layer.
       @param layer The layer to add.
     */
    public void addLayer(ViewLayer<? super T> layer) {
        layers.add(layer);
        layer.setWorldModel(world);
    }

    /**
       Remove a view layer.
       @param layer The layer to remove.
     */
    public void removeLayer(ViewLayer<? super T> layer) {
        layers.remove(layer);
    }

    /**
       Remove all view layers.
     */
    public void removeAllLayers() {
        layers.clear();
    }

    /**
       Set the world model that is to be viewed.
       @param newModel The new world model.
     */
    public void setWorldModel(T newModel) {
        this.world = newModel;
        for (ViewLayer<? super T> next : layers) {
            next.setWorldModel(world);
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
        prepaint(layers);
        for (ViewLayer<? super T> next : layers) {
            Graphics2D copy = (Graphics2D)g.create(insets.left, insets.top, width, height);
            objects.addAll(next.render(copy, width, height));
        }
        postpaint(layers);
    }

    /**
       Get all installed layers.
       @return All installed layers.
    */
    protected List<ViewLayer<? super T>> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    /**
       Do whatever needs doing before the layers are painted. The default implementation does nothing.
       @param allLayers The active layers.
     */
    protected void prepaint(Collection<ViewLayer<? super T>> allLayers) {
    }

    /**
       Do whatever needs doing after the layers are painted. The default implementation does nothing.
       @param allLayers The active layers.
     */
    protected void postpaint(Collection<ViewLayer<? super T>> allLayers) {
    }

    private List<RenderedObject> getObjectsAtPoint(int x, int y) {
        System.out.println("Getting objects at : " + x + ", " + y);
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
            System.out.println("Click at " + p);
            Insets insets = getInsets();
            List<RenderedObject> clicked = getObjectsAtPoint(p.x - insets.left, p.y - insets.top);
            for (RenderedObject next : clicked) {
                System.out.println(next.getObject());
            }
            fireObjectsClicked(clicked);
        }
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e) {}
    }
}