package maps.gml.view;

import maps.gml.GMLNode;
import rescuecore2.misc.gui.ScreenTransform;
import java.awt.Graphics2D;
import java.awt.Color;

/**
   A NodeDecorator that draws a cross for each node.
*/
public class CrossNodeDecorator implements NodeDecorator {
    private Color colour;
    private int size;

    /**
       Construct a CrossNodeDecorator.
       @param colour The colour to draw the cross.
       @param size The size of each arm of the cross.
    */
    public CrossNodeDecorator(Color colour, int size) {
        this.colour = colour;
        this.size = size;
    }

    @Override
    public void decorate(GMLNode node, Graphics2D g, ScreenTransform transform) {
        int x = transform.xToScreen(node.getX());
        int y = transform.yToScreen(node.getY());
        g.setColor(colour);
        g.drawLine(x - size, y - size, x + size, y + size);
        g.drawLine(x - size, y + size, x + size, y - size);
    }
}