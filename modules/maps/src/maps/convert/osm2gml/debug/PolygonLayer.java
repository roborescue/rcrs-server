package maps.convert.osm2gml.debug;

import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * A debug layer that displays a collection of polygonal objects.
 *
 * @param <T> the type of polygonal object in this layer
 */
public class PolygonLayer<T extends Polygonal> extends ShapeLayer<T> {
    private Color outlineColor;
    private Color fillColor;
    private int strokeWidth;

    public static final Color DEFAULT_OUTLINE_COLOR = Color.BLACK;
    public static final Color DEFAULT_FILL_COLOR = Color.GRAY;
    public static final int DEFAULT_STROKE_WIDTH = 2;

    private PolygonLayer(final Collection<T> objects) {
        super(objects);

        outlineColor = DEFAULT_OUTLINE_COLOR;
        fillColor = DEFAULT_FILL_COLOR;
        strokeWidth = DEFAULT_STROKE_WIDTH;
    }

    /**
     * Returns a new {@code PolygonLayer} containing the specified objects.
     *
     * @param objects the objects
     * @param <T> the type of polygonal object
     * @return a new {@code PolygonLayer}
     */
    public static <T extends Polygonal> PolygonLayer<T> of(final Collection<T> objects) {
        return new PolygonLayer<>(objects);
    }

    /**
     * Sets the name of this layer.
     *
     * @param name the layer name
     * @return this layer
     */
    public PolygonLayer<T> name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the outline color.
     *
     * @param outlineColor the outline color
     * @return this layer
     */
    public PolygonLayer<T> outlineColor(final Color outlineColor) {
        this.outlineColor = outlineColor;
        return this;
    }

    /**
     * Sets the fill color.
     *
     * @param fillColor the fill color
     * @return this layer
     */
    public PolygonLayer<T> fillColor(final Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    /**
     * Sets the outline stroke width.
     *
     * @param strokeWidth the stroke width
     * @return this layer
     */
    public PolygonLayer<T> strokeWidth(final int strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ShapeDebugFrame.ShapeInfo> createShapes() {
        return objects.stream().map(this::createShape).toList();
    }

    private ShapeDebugFrame.ShapeInfo createShape(final T object) {
        return new PolygonShapeInfo(object, name, outlineColor, fillColor, strokeWidth);
    }
}
