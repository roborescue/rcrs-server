package rescuecore2.standard.view;

import java.util.Collection;

import rescuecore2.view.ViewLayer;
import rescuecore2.view.WorldModelViewer;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.misc.gui.PanZoomListener;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A viewer for StandardWorldModels.
 */
public class StandardWorldModelViewer extends WorldModelViewer<StandardWorldModel> {
    private PanZoomListener panZoom;
    private ScreenTransform transform;

    /**
       Construct a standard world model viewer.
     */
    public StandardWorldModelViewer() {
        addDefaultLayers();
        panZoom = new PanZoomListener(this);
    }

    @Override
    public void setWorldModel(StandardWorldModel model) {
        super.setWorldModel(model);
        world.index();
        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> bounds = world.getWorldBounds();
        double xMin = bounds.first().first();
        double yMin = bounds.first().second();
        double xMax = bounds.second().first();
        double yMax = bounds.second().second();
        transform = new ScreenTransform(xMin, yMin, xMax, yMax);
        panZoom.setScreenTransform(transform);
        for (ViewLayer<? super StandardWorldModel> next : getLayers()) {
            if (next instanceof StandardViewLayer) {
                ((StandardViewLayer)next).setTransform(transform);
            }
        }
    }

    @Override
    public void addLayer(ViewLayer<? super StandardWorldModel> layer) {
        super.addLayer(layer);
        if (layer instanceof StandardViewLayer) {
            ((StandardViewLayer)layer).setTransform(transform);
        }
    }


    @Override
    protected void prepaint(Collection<ViewLayer<? super StandardWorldModel>> layers) {
        transform.rescale(getWidth(), getHeight());
    }

    /**
       Add the default layer set, i.e. nodes, roads, buildings and humans.
     */
    public void addDefaultLayers() {
        addLayer(new NodeLayer());
        addLayer(new RoadLayer());
        addLayer(new BuildingLayer());
        addLayer(new HumanLayer());
    }
}