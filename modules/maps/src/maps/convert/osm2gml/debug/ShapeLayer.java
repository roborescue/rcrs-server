package maps.convert.osm2gml.debug;

import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public abstract class ShapeLayer<T> {
    protected final Collection<T> objects;
    protected String name = "No name layer";

    protected ShapeLayer(final Collection<T> objects) {
        this.objects = objects;
    }

    public abstract List<ShapeDebugFrame.ShapeInfo> createShapes();

    @Override
    public String toString() {
        return String.format("%s (%d shapes)", name, objects.size());
    }
}
