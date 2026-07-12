package maps.convert.osm2gml.debug;

import maps.convert.osm2gml.Constants;
import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class PolygonLayer<T extends Polygonal> extends ShapeLayer<T> {
    private Color outlineColor;
    private Color fillColor;

    private PolygonLayer(final Collection<T> objects) {
        super(objects);

        this.outlineColor = Color.BLACK;
        this.fillColor    = Constants.TRANSPARENT_BLACK;
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

    public PolygonLayer<T> name(final String name) {
        this.name = name;
        return this;
    }

    public PolygonLayer<T> outlineColor(final Color outlineColor) {
        this.outlineColor = outlineColor;
        return this;
    }

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
