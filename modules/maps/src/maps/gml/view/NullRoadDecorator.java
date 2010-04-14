package maps.gml.view;

import maps.gml.GMLRoad;
import rescuecore2.misc.gui.ScreenTransform;
import java.awt.Graphics2D;

/**
   A no-op RoadDecorator.
*/
public class NullRoadDecorator implements RoadDecorator {
    @Override
    public void decorate(GMLRoad road, Graphics2D g, ScreenTransform transform) {
    }
}