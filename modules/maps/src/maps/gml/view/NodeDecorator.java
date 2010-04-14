package maps.gml.view;

import maps.gml.GMLNode;
import rescuecore2.misc.gui.ScreenTransform;
import java.awt.Graphics2D;

/**
   Interface for objects that know how to decorate GMLNodes.
*/
public interface NodeDecorator {
    /**
       Decorate a GMLNode.
       @param node The node to decorate.
       @param g The graphics to draw on.
       @param transform The screen transform.
    */
    void decorate(GMLNode node, Graphics2D g, ScreenTransform transform);
}