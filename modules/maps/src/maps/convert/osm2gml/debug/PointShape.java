package maps.convert.osm2gml.debug;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * The shape used to render a point in debug visualizations.
 */
public enum PointShape {

    DOT {
        @Override
        public Shape paint(final Graphics2D g, final int x, final int y, final int size) {
            final Shape shape = new Ellipse2D.Double(x - size / 2.0, y - size / 2.0, size, size);
            g.fill(shape);
            return shape;
        }
    },
    SQUARE {
        @Override
        public Shape paint(final Graphics2D g, final int x, final int y, final int size) {
            final Shape shape = new Rectangle.Double(x - size / 2.0, y - size / 2.0, size, size);
            g.fill(shape);
            return shape;
        }
    },
    CROSS {
        @Override
        public Shape paint(final Graphics2D g, final int x, final int y, final int size) {
            final int half = size / 2;
            final Path2D.Double shape = new Path2D.Double();
            shape.moveTo(x - half, y - half);
            shape.lineTo(x + half, y + half);
            shape.moveTo(x - half, y + half);
            shape.lineTo(x + half, y - half);
            g.draw(shape);
            return shape;
        }
    };

    /**
     * Paint this shape onto the given graphics context, centered at the given screen coordinates.
     * The caller is responsible for setting the color on {@code g} beforehand.
     *
     * @param g    The graphics context to paint onto.
     * @param x    The screen x coordinate of the shape's center.
     * @param y    The screen y coordinate of the shape's center.
     * @param size The overall size of the shape, in pixels.
     * @return The AWT shape that was painted, e.g. for use as bounds.
     */
    public abstract Shape paint(Graphics2D g, int x, int y, int size);
}
