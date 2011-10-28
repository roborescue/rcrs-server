package rescuecore2.standard.view;

import rescuecore2.view.LayerViewComponent;

/**
   A viewer for StandardWorldModels.
 */
public class StandardWorldModelViewer extends LayerViewComponent {
    /** Prefix for setting related to the StandardWorldModelViewer. */
    public static final String STANDARD_VIEWER_PREFIX = "viewer.standard";

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
        addLayer(new BuildingLayer());
        addLayer(new RoadLayer());
        addLayer(new AreaNeighboursLayer());
        addLayer(new RoadBlockageLayer());
        addLayer(new AreaIconLayer());
        addLayer(new HumanLayer());
        addLayer(new CommandLayer());
        addLayer(new PositionHistoryLayer());
    }
}
