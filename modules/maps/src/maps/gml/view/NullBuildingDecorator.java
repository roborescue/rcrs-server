package maps.gml.view;

import maps.gml.GMLBuilding;
import rescuecore2.misc.gui.ScreenTransform;
import java.awt.Graphics2D;

/**
   A no-op BuildingDecorator.
*/
public class NullBuildingDecorator implements BuildingDecorator {
    @Override
    public void decorate(GMLBuilding building, Graphics2D g, ScreenTransform transform) {
    }
}