package rescuecore2.view;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import rescuecore2.misc.gui.ScreenTransform;

/**
   A ViewComponent that uses layers.
 */
public class LayerViewComponent extends ViewComponent {
    private List<ViewLayer> layers;
    private Object[] data;
    private Rectangle2D bounds;

    /**
       Construct a new LayerViewComponent.
     */
    public LayerViewComponent() {
        layers = new ArrayList<ViewLayer>();
    }

    /**
       Add a view layer.
       @param layer The layer to add.
     */
    public void addLayer(ViewLayer layer) {
        layers.add(layer);
        computeBounds();
    }

    /**
       Remove a view layer.
       @param layer The layer to remove.
     */
    public void removeLayer(ViewLayer layer) {
        layers.remove(layer);
        computeBounds();
    }

    /**
       Remove all view layers.
     */
    public void removeAllLayers() {
        layers.clear();
        computeBounds();
    }

    @Override
    public void view(Object... objects) {
        data = objects;
        computeBounds();
        super.view(objects);
    }

    @Override
    protected Collection<RenderedObject> render(Graphics2D g, ScreenTransform transform, int width, int height) {
        Collection<RenderedObject> result = new HashSet<RenderedObject>();
        prepaint();
        for (ViewLayer next : layers) {
            Graphics2D copy = (Graphics2D)g.create();
            result.addAll(next.render(copy, transform, width, height));
        }
        postpaint();
        return result;
    }

    /**
       Get all installed layers.
       @return All installed layers.
    */
    protected List<ViewLayer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    /**
       Do whatever needs doing before the layers are painted. The default implementation does nothing.
     */
    protected void prepaint() {
    }

    /**
       Do whatever needs doing after the layers are painted. The default implementation does nothing.
     */
    protected void postpaint() {
    }

    private void computeBounds() {
        Rectangle2D oldBounds = bounds;
        bounds = null;
        for (ViewLayer next : layers) {
            expandBounds(next.view(data));
        }
        if (bounds == null) {
            updateBounds(0, 0, 1, 1);
        }
        else if (oldBounds == null
                 || oldBounds.getMinX() != bounds.getMinX()
                 || oldBounds.getMinY() != bounds.getMinY()
                 || oldBounds.getMaxX() != bounds.getMaxX()
                 || oldBounds.getMaxY() != bounds.getMaxY()) {
            updateBounds(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());
        }
    }

    private void expandBounds(Rectangle2D next) {
        if (next == null) {
            return;
        }
        if (bounds == null) {
            bounds = next;
        }
        else {
            Rectangle2D.union(bounds, next, bounds);
        }
    }
}