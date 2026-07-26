package maps.convert.osm2gml.debug;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * A debug layer that displays a collection of point-like objects.
 * @param <T> The type of point-like object in this layer.
 */
public class PointLayer<T> extends ShapeLayer<T> {
    private Color color;
    private PointShape shape;
    private int size;

    public static final Color      DEFAULT_COLOR = Color.BLACK;
    public static final PointShape DEFAULT_SHAPE = PointShape.DOT;
    public static final int        DEFAULT_SIZE  = 8;

    private PointLayer(final Collection<T> objects) {
        super(objects);

        this.color = DEFAULT_COLOR;
        this.shape = DEFAULT_SHAPE;
        this.size  = DEFAULT_SIZE;
    }

    /**
     * Create a new {@code PointLayer}.
     * @param objects The point-like objects to display.
     * @param <T>     The type of object in this layer.
     * @return A new {@code PointLayer}.
     */
    public static <T> PointLayer<T> of(final Collection<T> objects) {
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
     * Set the point shape.
     * @param shape The point shape.
     * @return This layer, for chaining.
     */
    @SuppressWarnings("unused")
    public PointLayer<T> shape(final PointShape shape) {
        this.shape = shape;
        return this;
    }

    /**
     * Set the point size.
     * @param size The point size.
     * @return This layer, for chaining.
     */
    @SuppressWarnings("unused")
    public PointLayer<T> size(final int size) {
        this.size = size;
        return this;
    }

    @Override
    public List<ShapeDebugFrame.ShapeInfo> createShapes() {
        return objects.stream().map(this::createShape).toList();
    }

    private ShapeDebugFrame.ShapeInfo createShape(final T object) {
        return switch (object) {
            case Puntal puntal -> new PointShapeInfo(puntal, name, color, shape, size);
            case Point2D point -> new PointShapeInfo(point, name, color, shape, size);
            default -> throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
        };
    }
}
