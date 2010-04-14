package maps.gml.view;

import maps.gml.GMLSpace;
import rescuecore2.misc.gui.ScreenTransform;
import java.awt.Graphics2D;

/**
   Interface for objects that know how to decorate GMLSpaces.
*/
public interface SpaceDecorator {
    /**
       Decorate a GMLSpace.
       @param space The space to decorate.
       @param g The graphics to draw on.
       @param transform The screen transform.
    */
    void decorate(GMLSpace space, Graphics2D g, ScreenTransform transform);
}