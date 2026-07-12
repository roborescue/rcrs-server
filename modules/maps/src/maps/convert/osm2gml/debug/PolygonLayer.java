package maps.convert.osm2gml.debug;

import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * A debug layer that displays a collection of polygonal objects.
 * @param <T> The type of polygonal object in this layer.
 */
public class PolygonLayer<T extends Polygonal> extends ShapeLayer<T> {
    private Color outlineColor;
    private Color fillColor;

    public static final Color DEFAULT_OUTLINE_COLOR = Color.BLACK;
    public static final Color DEFAULT_FILL_COLOR    = Color.GRAY;

    private PolygonLayer(final Collection<T> objects) {
        super(objects);

        this.outlineColor = DEFAULT_OUTLINE_COLOR;
        this.fillColor    = DEFAULT_FILL_COLOR;
    }

    /**
     * Create a new {@code PolygonLayer}.
     * @param objects The polygonal objects to display.
     * @param <T>     The type of object in this layer.
     * @return A new {@code PolygonLayer}.
     */
    public static <T extends Polygonal> PolygonLayer<T> of(final Collection<T> objects) {
        return new PolygonLayer<>(objects);
    }

    /**
     * Set the display name of this layer.
     * @param name The layer name.
     * @return This layer, for chaining.
     */
    public PolygonLayer<T> name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the polygon outline color.
     * @param outlineColor The outline color.
     * @return This layer, for chaining.
     */
    public PolygonLayer<T> outlineColor(final Color outlineColor) {
        this.outlineColor = outlineColor;
        return this;
    }

    /**
     * Set the polygon fill color.
     * @param fillColor The fill color.
     * @return This layer, for chaining.
     */
    public PolygonLayer<T> fillColor(final Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    @Override
    public List<ShapeDebugFrame.ShapeInfo> createShapes() {
        return objects.stream().map(this::createShape).toList();
    }

    private ShapeDebugFrame.ShapeInfo createShape(final T object) {
        return new PolygonShapeInfo(object, name, outlineColor, fillColor);
    }
}
