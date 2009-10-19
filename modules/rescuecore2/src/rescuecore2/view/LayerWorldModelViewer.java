package rescuecore2.view;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

import java.awt.Graphics2D;

import rescuecore2.misc.gui.ScreenTransform;

/**
   A WorldModelViewer that uses layers.
 */
public abstract class LayerWorldModelViewer extends WorldModelViewer {
    private List<ViewLayer> layers;

    /**
       Construct a new LayerWorldModelViewer.
     */
    public LayerWorldModelViewer() {
        layers = new ArrayList<ViewLayer>();
    }

    /**
       Add a view layer.
       @param layer The layer to add.
     */
    public void addLayer(ViewLayer layer) {
        layers.add(layer);
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

    @Override
    protected Collection<RenderedObject> render(Graphics2D g, ScreenTransform transform, int width, int height) {
        Collection<RenderedObject> result = new HashSet<RenderedObject>();
        prepaint();
        for (ViewLayer next : layers) {
            Graphics2D copy = (Graphics2D)g.create();
            result.addAll(next.render(copy, transform, width, height, world, commands, updates));
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
}