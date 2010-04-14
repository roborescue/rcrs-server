package maps.gml.view;

import maps.gml.GMLNode;
import rescuecore2.misc.gui.ScreenTransform;
import java.awt.Graphics2D;

/**
   A no-op NodeDecorator.
*/
public class NullNodeDecorator implements NodeDecorator {
    @Override
    public void decorate(GMLNode node, Graphics2D g, ScreenTransform transform) {
    }
}