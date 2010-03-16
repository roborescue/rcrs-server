package maps.convert.osm2gml;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Polygon;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import java.util.List;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.gui.ShapeDebugFrame;

import maps.gml.GMLCoordinates;

/**
   A ShapeInfo that knows how to draw TemporaryObjects.
*/
public class TemporaryObjectInfo extends ShapeDebugFrame.ShapeInfo {
    private TemporaryObject shape;
    private Color outlineColour;
    private Color fillColour;
    private Rectangle2D bounds;

    /**
       Create a new TemporaryObjectInfo.
       @param shape The shape to draw.
       @param name The name of the shape.
       @param outlineColour The colour to draw the outline of the shape. This may be null to indicate that the outline should not be painted.
       @param fillColour The colour to draw the interior of the shape. This may be null to indicate that the interior should not be painted.
     */
    public TemporaryObjectInfo(TemporaryObject shape, String name, Color outlineColour, Color fillColour)  {
        super(shape, name);
        this.shape = shape;
        this.outlineColour = outlineColour;
        this.fillColour = fillColour;
        if (shape != null) {
            bounds = shape.getBounds();
        }
    }

    @Override
    public Shape paint(Graphics2D g, ScreenTransform transform) {
        if (shape == null) {
            return null;
        }
        List<GMLCoordinates> coordinates = shape.makeGMLCoordinates();
        int n = coordinates.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        int i = 0;
        for (GMLCoordinates next : coordinates) {
            xs[i] = transform.xToScreen(next.getX());
            ys[i] = transform.yToScreen(next.getY());
            ++i;
        }
        Polygon p = new Polygon(xs, ys, n);
        if (fillColour != null) {
            g.setColor(fillColour);
            g.fill(p);
        }
        if (outlineColour != null) {
            g.setColor(outlineColour);
            g.draw(p);
        }
        return p;
    }

    @Override
    public void paintLegend(Graphics2D g, int width, int height) {
        if (outlineColour != null) {
            g.setColor(outlineColour);
            g.drawRect(0, 0, width - 1, height - 1);
        }
        if (fillColour != null) {
            g.setColor(fillColour);
            g.fillRect(0, 0, width, height);
        }
    }

    @Override
    public Rectangle2D getBoundsShape() {
        return bounds;
    }

    @Override
    public java.awt.geom.Point2D getBoundsPoint() {
        return null;
    }
}
