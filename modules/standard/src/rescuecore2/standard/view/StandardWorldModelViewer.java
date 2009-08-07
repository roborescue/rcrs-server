package rescuecore2.standard.view;

import rescuecore2.view.LayerViewComponent;

/**
   A viewer for StandardWorldModels.
 */
public class StandardWorldModelViewer extends LayerViewComponent {
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

    /**
       Add the default layer set, i.e. nodes, roads, buildings, humans and commands.
     */
    public void addDefaultLayers() {
	addLayer(new AreaLayer());
        addLayer(new BuildingLayer());
        addLayer(new RoadLayer());
        addLayer(new NodeLayer());
        addLayer(new RoadBlockageLayer());
        addLayer(new BuildingIconLayer());
        addLayer(new HumanLayer());
        addLayer(new CommandLayer());
    }
}