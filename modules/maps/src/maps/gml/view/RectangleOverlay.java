package maps.gml.view;

import java.awt.Graphics2D;
import java.awt.Color;

import rescuecore2.misc.gui.ScreenTransform;

/**
   A rectangular overlay.
 */
public class RectangleOverlay implements Overlay {
    private double left;
    private double right;
    private double bottom;
    private double top;
    private Color colour;
    private boolean useWorldCoords;

    /**
       Construct a RectangleOverlay with no coordinates defined..
       @param colour The colour to draw the rectangle.
       @param useWorldCoords Whether to convert the coordinates from world to screen. Set to false if you want to directly specify screen coordinates.
    */
    public RectangleOverlay(Color colour, boolean useWorldCoords) {
        this(Double.NaN, Double.NaN, Double.NaN, Double.NaN, colour, useWorldCoords);
    }

    /**
       Construct a RectangleOverlay.
       @param left The left-hand X coordinate.
       @param right The right-hand X coordinate.
       @param top The top Y coordinate.
       @param bottom The bottom Y coordinate.
       @param colour The colour to draw the rectangle.
       @param useWorldCoords Whether to convert the coordinates from world to screen. Set to false if you want to directly specify screen coordinates.
    */
    public RectangleOverlay(double left, double right, double top, double bottom, Color colour, boolean useWorldCoords) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.colour = colour;
        this.useWorldCoords = useWorldCoords;
    }

    /**
       Set the left-hand X coordinate.
       @param x The new coordinate.
    */
    public void setLeft(double x) {
        left = x;
    }

    /**
       Set the right-hand X coordinate.
       @param x The new coordinate.
    */
    public void setRight(double x) {
        right = x;
    }

    /**
       Set the top Y coordinate.
       @param y The new coordinate.
    */
    public void setTop(double y) {
        top = y;
    }

    /**
       Set the bottom Y coordinate.
       @param y The new coordinate.
    */
    public void setBottom(double y) {
        bottom = y;
    }

    /**
       Set the colour.
       @param c The new colour.
    */
    public void setColour(Color c) {
        colour = c;
    }

    /**
       Set whether to use world coordinates or not. If true, the coordinates will be converted to screen coordinates; if false the coordinates will be used as given.
       @param b True to use world coordinates, false otherwise.
    */
    public void setUseWorldCoordinates(boolean b) {
        useWorldCoords = b;
    }

    @Override
    public void render(Graphics2D g, ScreenTransform transform) {
        if (Double.isNaN(left) || Double.isNaN(right) || Double.isNaN(top) || Double.isNaN(bottom)) {
            return;
        }
        Graphics2D graphics = (Graphics2D)g.create();
        graphics.setColor(colour);
        double x1 = left < right ? left : right;
        double x2 = left < right ? right : left;
        double y1 = bottom < top ? bottom : top;
        double y2 = bottom < top ? top : bottom;
        if (useWorldCoords) {
            x1 = transform.xToScreen(x1);
            x2 = transform.xToScreen(x2);
            double temp = transform.yToScreen(y2);
            y2 = transform.yToScreen(y1);
            y1 = temp;
        }
        graphics.fillRect((int)x1, (int)y1, (int)(x2 - x1), (int)(y2 - y1));
    }
}