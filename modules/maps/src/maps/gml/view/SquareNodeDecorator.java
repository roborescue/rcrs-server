package maps.gml.view;

import maps.gml.GMLNode;
import rescuecore2.misc.gui.ScreenTransform;
import java.awt.Graphics2D;
import java.awt.Color;

/**
   A NodeDecorator that draws a square for each node.
*/
public class SquareNodeDecorator implements NodeDecorator {
    private Color colour;
    private int size;

    /**
       Construct a SquareNodeDecorator.
       @param colour The colour to draw the square.
       @param size The size of the square.
    */
    public SquareNodeDecorator(Color colour, int size) {
        this.colour = colour;
        this.size = size;
    }

    @Override
    public void decorate(GMLNode node, Graphics2D g, ScreenTransform transform) {
        int x = transform.xToScreen(node.getX());
        int y = transform.yToScreen(node.getY());
        g.setColor(colour);
        g.fillRect(x - (size / 2), y - (size / 2), size + 1, size + 1);
    }
}