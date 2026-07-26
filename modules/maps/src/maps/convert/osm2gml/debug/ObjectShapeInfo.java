package maps.convert.osm2gml.debug;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Polygon;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import java.util.List;

import maps.convert.osm2gml.TemporaryObject;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.gui.ShapeDebugFrame;

import maps.gml.GMLCoordinates;

/**
 * A {@link rescuecore2.misc.gui.ShapeDebugFrame.ShapeInfo} for debugging a {@link TemporaryObject}.
 */
public class ObjectShapeInfo extends ShapeDebugFrame.ShapeInfo {
    private final TemporaryObject shape;
    private final Color outlineColour;
    private final Color fillColour;
    private Rectangle2D bounds;

    /**
     * Construct a new {@code ObjectShapeInfo}.
     * @param shape        The object to display.
     * @param name         The name of the object.
     * @param outlineColor The color of the outline of the object.
     * @param fillColor    The color to fill the object.
     */
    public ObjectShapeInfo(
            final TemporaryObject shape, final String name, final Color outlineColor, final Color fillColor)  {
        super(shape, name);
        this.shape         = shape;
        this.outlineColour = outlineColor;
        this.fillColour    = fillColor;
        if (shape != null) bounds = shape.getBounds();
    }

    @Override
    public Shape paint(final Graphics2D g, final ScreenTransform transform) {
        if (shape == null) return null;

        final List<GMLCoordinates> coordinates = shape.makeGMLCoordinates();
        final int n = coordinates.size();
        final int[] xs = new int[n];
        final int[] ys = new int[n];
        int i = 0;
        for (final GMLCoordinates next : coordinates) {
            xs[i] = transform.xToScreen(next.getX());
            ys[i] = transform.yToScreen(next.getY());
            i++;
        }

        final Polygon p = new Polygon(xs, ys, n);
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
    public void paintLegend(final Graphics2D g, final int width, final int height) {
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
