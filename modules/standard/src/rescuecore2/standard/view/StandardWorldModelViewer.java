package rescuecore2.standard.view;

import java.util.Collection;

import rescuecore2.view.LayerWorldModelViewer;
import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.Command;

import rescuecore2.standard.entities.StandardWorldModel;

/**
   A viewer for StandardWorldModels.
 */
public class StandardWorldModelViewer extends LayerWorldModelViewer {
    /**
       Construct a standard world model viewer.
     */
    public StandardWorldModelViewer() {
        addDefaultLayers();
    }

    @Override
    public String getViewerName() {
        return "Standard world model viewer";
    }

    @Override
    public void view(WorldModel<? extends Entity> model, Collection<Command> commands, Collection<Entity> updates) {
        StandardWorldModel world = StandardWorldModel.createStandardWorldModel(model);
        super.view(world, commands, updates);
        world.index();
        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> bounds = world.getWorldBounds();
        double xMin = bounds.first().first();
        double yMin = bounds.first().second();
        double xMax = bounds.second().first();
        double yMax = bounds.second().second();
        updateBounds(xMin, yMin, xMax, yMax);
    }

    /**
       Add the default layer set, i.e. nodes, roads, buildings and humans.
     */
    public void addDefaultLayers() {
        addLayer(new NodeLayer());
        addLayer(new RoadLayer());
        addLayer(new BuildingLayer());
        addLayer(new HumanLayer());
        addLayer(new CommandLayer());
    }
}