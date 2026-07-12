package maps.convert.osm2gml.debug;

import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class LineLayer<T extends Lineal> extends ShapeLayer<T> {
    private Color color;
    private boolean thick;
    private boolean arrow;

    private LineLayer(final Collection<T> objects) {
        super(objects);
    }

    public static <T extends Lineal> LineLayer<T> of(final Collection<T> objects) {
        return new LineLayer<>(objects);
    }

    public LineLayer<T> name(final String name) {
        this.name = name;
        return this;
    }

    @SuppressWarnings("unuse")
    public LineLayer<T> color(final Color color) {
        this.color = color;
        return this;
    }

    @SuppressWarnings("unuse")
    public LineLayer<T> thick(final boolean thick) {
        this.thick = thick;
        return this;
    }

    @SuppressWarnings("unuse")
    public LineLayer<T> arrow(final boolean arrow) {
        this.arrow = arrow;
        return this;
    }

    @Override
    public List<ShapeDebugFrame.ShapeInfo> createShapes() {
        return objects.stream().map(this::createShape).toList();
    }

    private ShapeDebugFrame.ShapeInfo createShape(final T object) {
        return new LineShapeInfo(object, name, color, thick, arrow);
    }
}
