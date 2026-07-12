package maps.convert.osm2gml.debug;

import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class PointLayer<T extends Puntal> extends ShapeLayer<T> {
    private Color color;
    private boolean square;

    private PointLayer(final Collection<T> objects) {
        super(objects);

        this.color  = Color.BLACK;
        this.square = true;
    }

    public static <T extends Puntal> PointLayer<T> of(final Collection<T> objects) {
        return new PointLayer<>(objects);
    }

    public PointLayer<T> name(final String name) {
        this.name = name;
        return this;
    }

    @SuppressWarnings("unused")
    public PointLayer<T> color(final Color color) {
        this.color = color;
        return this;
    }

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
