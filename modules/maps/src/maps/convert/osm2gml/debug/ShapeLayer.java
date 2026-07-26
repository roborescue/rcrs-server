package maps.convert.osm2gml.debug;

import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * Base class for a layer of debug shapes displayed together in a {@link ShapeDebugFrame}.
 * @param <T> The type of object contained in this layer.
 */
public abstract class ShapeLayer<T> {
    protected final Collection<T> objects;
    protected String name = "No name layer";

    protected ShapeLayer(final Collection<T> objects) {
        this.objects = objects;
    }

    /**
     * Build the {@link rescuecore2.misc.gui.ShapeDebugFrame.ShapeInfo} instances used to render this layer.
     * @return The shapes to display for this layer.
     */
    public abstract List<ShapeDebugFrame.ShapeInfo> createShapes();

    @Override
    public String toString() {
        return String.format("%s (%d shapes)", name, objects.size());
    }
}
