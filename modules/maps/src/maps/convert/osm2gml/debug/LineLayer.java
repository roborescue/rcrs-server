package maps.convert.osm2gml.debug;

import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * A debug layer that displays a collection of linear objects.
 * @param <T> The type of linear object in this layer.
 */
public class LineLayer<T extends Lineal> extends ShapeLayer<T> {
    private Color color;
    private boolean thick;
    private boolean arrow;
    private boolean dashed;

    public static final Color   DEFAULT_COLOR  = Color.BLACK;
    public static final boolean DEFAULT_THICK  = false;
    public static final boolean DEFAULT_ARROW  = false;
    public static final boolean DEFAULT_DASHED = false;

    private LineLayer(final Collection<T> objects) {
        super(objects);

        this.color  = DEFAULT_COLOR;
        this.thick  = DEFAULT_THICK;
        this.arrow  = DEFAULT_ARROW;
        this.dashed = DEFAULT_DASHED;
    }

    /**
     * Create a new {@code LineLayer}.
     * @param objects The linear objects to display.
     * @param <T>     The type of object in this layer.
     * @return A new {@code LineLayer}.
     */
    public static <T extends Lineal> LineLayer<T> of(final Collection<T> objects) {
        return new LineLayer<>(objects);
    }

    /**
     * Set the display name of this layer.
     * @param name The layer name.
     * @return This layer, for chaining.
     */
    @SuppressWarnings("unused")
    public LineLayer<T> name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the line color.
     * @param color The line color.
     * @return This layer, for chaining.
     */
    @SuppressWarnings("unused")
    public LineLayer<T> color(final Color color) {
        this.color = color;
        return this;
    }

    /**
     * Set whether lines are drawn thick.
     * @param thick {@code true} to draw thick lines.
     * @return This layer, for chaining.
     */
    @SuppressWarnings("unused")
    public LineLayer<T> thick(final boolean thick) {
        this.thick = thick;
        return this;
    }

    /**
     * Set whether lines are drawn with an arrowhead indicating direction.
     * @param arrow {@code true} to draw an arrowhead.
     * @return This layer, for chaining.
     */
    @SuppressWarnings("unused")
    public LineLayer<T> arrow(final boolean arrow) {
        this.arrow = arrow;
        return this;
    }

    /**
     * Set whether lines are drawn dashed.
     * @param dashed {@code true} to draw dashed lines.
     * @return This layer, for chaining.
     */
    @SuppressWarnings("unused")
    public LineLayer<T> dashed(final boolean dashed) {
        this.dashed = dashed;
        return this;
    }

    @Override
    public List<ShapeDebugFrame.ShapeInfo> createShapes() {
        return objects.stream().map(this::createShape).toList();
    }

    private ShapeDebugFrame.ShapeInfo createShape(final T object) {
        return new LineShapeInfo(object, name, color, thick, arrow, dashed);
    }
}
