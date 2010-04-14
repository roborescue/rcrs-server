package maps.gml.view;

import maps.gml.GMLEdge;
import rescuecore2.misc.gui.ScreenTransform;
import java.awt.Graphics2D;

/**
   Interface for objects that know how to decorate GMLEdges.
*/
public interface EdgeDecorator {
    /**
       Decorate a GMLEdge.
       @param edge The edge to decorate.
       @param g The graphics to draw on.
       @param transform The screen transform.
    */
    void decorate(GMLEdge edge, Graphics2D g, ScreenTransform transform);
}