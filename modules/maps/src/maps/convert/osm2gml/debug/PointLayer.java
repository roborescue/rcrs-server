package maps.convert.osm2gml.debug;

import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * A debug layer that displays a collection of point-like objects.
 * @param <T> The type of point-like object in this layer.
 */
public class PointLayer<T extends Puntal> extends ShapeLayer<T> {
    private Color color;
    private boolean square;

    public static final Color   DEFAULT_COLOR  = Color.BLACK;
    public static final boolean DEFAULT_SQUARE = true;

    private PointLayer(final Collection<T> objects) {
        super(objects);

        this.color  = DEFAULT_COLOR;
        this.square = DEFAULT_SQUARE;
    }

    /**
     * Create a new {@code PointLayer}.
     * @param objects The point-like objects to display.
     * @param <T>     The type of object in this layer.
     * @return A new {@code PointLayer}.
     */
    public static <T extends Puntal> PointLayer<T> of(final Collection<T> objects) {
        return new PointLayer<>(objects);
    }

    /**
     * Set the display name of this layer.
     * @param name The layer name.
     * @return This layer, for chaining.
     */
    @SuppressWarnings("unused")
    public PointLayer<T> name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the point color.
     * @param color The point color.
     * @return This layer, for chaining.
     */
    @SuppressWarnings("unused")
    public PointLayer<T> color(final Color color) {
        this.color = color;
        return this;
    }

    /**
     * Set whether points are drawn as squares rather than circles.
     * @param square {@code true} to draw square markers.
     * @return This layer, for chaining.
     */
    @SuppressWarnings("unused")
    public PointLayer<T> square(final boolean square) {
        this.square = square;
        return this;
    }

    @Override
    public List<ShapeDebugFrame.ShapeInfo> createShapes() {
        return objects.stream().map(this::createShape).toList();
    }

    private ShapeDebugFrame.ShapeInfo createShape(final T object) {
        return new PointShapeInfo(object, name, color, square);
    }
}
