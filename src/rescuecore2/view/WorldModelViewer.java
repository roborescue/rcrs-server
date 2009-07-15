package rescuecore2.view;

import java.util.List;
import java.util.ArrayList;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

/**
   A JComponent that shows a view of a world model.
 */
public class WorldModelViewer extends JComponent {
    private static final Color BACKGROUND = new Color(0x1B898D);

    private List<ViewLayer> layers;
    private List<RenderedObject> objects;
    private List<ViewListener> listeners;
    private WorldModel<? extends Entity> world;

    /**
       Construct a new WorldModelViewer.
     */
    public WorldModelViewer() {
        layers = new ArrayList<ViewLayer>();
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
    public void addLayer(ViewLayer layer) {
        layers.add(layer);
        layer.setWorldModel(world);
    }

    /**
       Remove a view layer.
       @param layer The layer to remove.
     */
    public void removeLayer(ViewLayer layer) {
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
    public void setWorldModel(WorldModel<? extends Entity> newModel) {
        this.world = newModel;
        for (ViewLayer next : layers) {
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
        for (ViewLayer next : layers) {
            Graphics copy = g.create(insets.left, insets.top, width, height);
            objects.addAll(next.render(copy, width, height));
        }
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
            System.out.println("Click at " + p);
            List<RenderedObject> clicked = getObjectsAtPoint(p.x, p.y);
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