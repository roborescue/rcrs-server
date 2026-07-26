package maps.convert.osm2gml.debug;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

public class PolygonShapeInfo extends ShapeDebugFrame.ShapeInfo {
    private final List<Point2D> vertices;
    private final Color outlineColour;
    private final Color fillColour;
    private final Stroke stroke;
    private final Rectangle2D bounds;

    /**
     * Construct a new {@code PolygonShapeInfo}.
     * @param polygon      The polygon to display.
     * @param name         The name of the polygon.
     * @param outlineColor The color of the outline of the polygon.
     * @param fillColor    The color to fill the polygon.
     */
    public PolygonShapeInfo(
            final Polygonal polygon, final String name, final Color outlineColor, final Color fillColor,
            final int strokeWidth)  {
        super(polygon, name);
        this.vertices      = polygon == null ? Collections.emptyList() : polygon.getVertices();
        this.outlineColour = outlineColor;
        this.fillColour    = fillColor;
        this.stroke        = new BasicStroke(strokeWidth);
        this.bounds        = computeBounds(vertices);
    }

    private static Rectangle2D computeBounds(final List<Point2D> vertices) {
        if (vertices.isEmpty()) return null;

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (final Point2D vertex : vertices) {
            minX = Math.min(minX, vertex.getX());
            minY = Math.min(minY, vertex.getY());
            maxX = Math.max(maxX, vertex.getX());
            maxY = Math.max(maxY, vertex.getY());
        }
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public Shape paint(final Graphics2D g, final ScreenTransform transform) {
        if (vertices.isEmpty()) return null;

        final int n = vertices.size();
        final int[] xs = new int[n];
        final int[] ys = new int[n];
        for (int i = 0; i < n; i++) {
            final Point2D vertex = vertices.get(i);
            xs[i] = transform.xToScreen(vertex.getX());
            ys[i] = transform.yToScreen(vertex.getY());
        }

        final Polygon polygon = new Polygon(xs, ys, n);
        if (fillColour != null) {
            g.setColor(fillColour);
            g.fill(polygon);
        }
        if (outlineColour != null) {
            g.setStroke(stroke);
            g.setColor(outlineColour);
            g.draw(polygon);
        }
        return polygon;
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
