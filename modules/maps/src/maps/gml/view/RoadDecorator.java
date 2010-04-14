package maps.gml.view;

import maps.gml.GMLRoad;
import rescuecore2.misc.gui.ScreenTransform;
import java.awt.Graphics2D;

/**
   Interface for objects that know how to decorate GMLRoads.
*/
public interface RoadDecorator {
    /**
       Decorate a GMLRoad.
       @param road The road to decorate.
       @param g The graphics to draw on.
       @param transform The screen transform.
    */
    void decorate(GMLRoad road, Graphics2D g, ScreenTransform transform);
}