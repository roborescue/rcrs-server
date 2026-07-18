package maps.convert.osm2gml.debug;

import lombok.Getter;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.awt.geom.Point2D;

public class PointShapeInfo extends ShapeDebugFrame.ShapeInfo {
    private final Puntal point;
    private final Color color;
    private final PointShape shape;
    private final int size;

    @Getter private final Point2D boundsPoint;

    public PointShapeInfo(Puntal point, String name, Color color,
                          final PointShape shape, final int size) {
        super(point, name);
        this.point = point;
        this.color = color;
        this.shape = shape;
        this.size = size;
        this.boundsPoint = new Point2D.Double(point.getPoint().getX(), point.getPoint().getY());
    }

    @Override
    public Shape paint(final Graphics2D g, final ScreenTransform transform) {
        g.setColor(color);

        final int x = transform.xToScreen(point.getPoint().getX());
        final int y = transform.yToScreen(point.getPoint().getY());
        return shape.paint(g, x, y, size);
    }

    @Override
    public void paintLegend(final Graphics2D g, final int width, final int height) {
        g.setColor(color);

        final int x = width / 2;
        final int y = height / 2;
        shape.paint(g, x, y, size);
    }

    @Override
    public Shape getBoundsShape() {
        return null;
    }
}
