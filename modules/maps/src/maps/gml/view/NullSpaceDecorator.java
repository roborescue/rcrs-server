package maps.gml.view;

import maps.gml.GMLSpace;
import rescuecore2.misc.gui.ScreenTransform;
import java.awt.Graphics2D;

/**
   A no-op SpaceDecorator.
*/
public class NullSpaceDecorator implements SpaceDecorator {
    @Override
    public void decorate(GMLSpace space, Graphics2D g, ScreenTransform transform) {
    }
}