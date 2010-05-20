package maps.gml.view;

import java.awt.Graphics2D;
import java.awt.Color;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.geometry.Point2D;

/**
   A line overlay.
 */
public class LineOverlay implements Overlay {
    private Point2D start;
    private Point2D end;
    private Color colour;
    private boolean useWorldCoords;

    /**
       Construct a LineOverlay with no coordinates defined..
       @param colour The colour to draw the rectangle.
       @param useWorldCoords Whether to convert the coordinates from world to screen. Set to false if you want to directly specify screen coordinates.
    */
    public LineOverlay(Color colour, boolean useWorldCoords) {
        this(null, null, colour, useWorldCoords);
    }

    /**
       Construct a LineOverlay.
       @param start The start coordinate.
       @param end The end coordinate.
       @param colour The colour to draw the rectangle.
       @param useWorldCoords Whether to convert the coordinates from world to screen. Set to false if you want to directly specify screen coordinates.
    */
    public LineOverlay(Point2D start, Point2D end, Color colour, boolean useWorldCoords) {
        this.start = start;
        this.end = end;
        this.colour = colour;
        this.useWorldCoords = useWorldCoords;
    }

    /**
       Set the start coordinate.
       @param p The new start coordinate.
    */
    public void setStart(Point2D p) {
        start = p;
    }

    /**
       Set the end coordinate.
       @param p The new end coordinate.
    */
    public void setEnd(Point2D p) {
        end = p;
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
        if (start == null || end == null) {
            return;
        }
        Graphics2D graphics = (Graphics2D)g.create();
        graphics.setColor(colour);
        double x1 = start.getX();
        double x2 = end.getX();
        double y1 = start.getY();
        double y2 = end.getY();
        if (useWorldCoords) {
            x1 = transform.xToScreen(x1);
            x2 = transform.xToScreen(x2);
            /*
            double temp = transform.yToScreen(y2);
            y2 = transform.yToScreen(y1);
            y1 = temp;
            */
            y1 = transform.yToScreen(y1);
            y2 = transform.yToScreen(y2);
        }
        graphics.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
    }
}