package maps.gml.view;

import maps.gml.GMLEdge;
import rescuecore2.misc.gui.ScreenTransform;
import java.awt.Graphics2D;

/**
   A no-op EdgeDecorator.
*/
public class NullEdgeDecorator implements EdgeDecorator {
    @Override
    public void decorate(GMLEdge edge, Graphics2D g, ScreenTransform transform) {
    }
}