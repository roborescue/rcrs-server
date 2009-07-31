package rescuecore2.standard.view;

import java.util.Collection;

import rescuecore2.view.ViewLayer;
import rescuecore2.view.WorldModelViewer;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.StandardWorldModel;

/**
   A viewer for StandardWorldModels.
 */
public class StandardWorldModelViewer extends WorldModelViewer<StandardWorldModel> {
    /**
       Construct a standard world model viewer.
     */
    public StandardWorldModelViewer() {
        addDefaultLayers();
    }

    @Override
    public void setWorldModel(StandardWorldModel model) {
        super.setWorldModel(model);
        world.index();
    }

    @Override
    protected void prepaint(Collection<ViewLayer<? super StandardWorldModel>> layers) {
        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> bounds = world.getWorldBounds();
        double xMin = bounds.first().first();
        double yMin = bounds.first().second();
        double xRange = bounds.second().first() - bounds.first().first();
        double yRange = bounds.second().second() - bounds.first().second();
        ScreenTransform transform = new ScreenTransform(xMin, yMin, xRange, yRange, getWidth(), getHeight());
        for (ViewLayer<? super StandardWorldModel> next : layers) {
            if (next instanceof StandardViewLayer) {
                ((StandardViewLayer)next).setTransform(transform);
            }
        }
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