package maps.gml.view;

import maps.gml.GMLBuilding;
import rescuecore2.misc.gui.ScreenTransform;
import java.awt.Graphics2D;

/**
   Interface for objects that know how to decorate GMLBuildings.
*/
public interface BuildingDecorator {
    /**
       Decorate a GMLBuilding.
       @param building The building to decorate.
       @param g The graphics to draw on.
       @param transform The screen transform.
    */
    void decorate(GMLBuilding building, Graphics2D g, ScreenTransform transform);
}