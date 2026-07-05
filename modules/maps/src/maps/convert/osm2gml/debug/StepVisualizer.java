package maps.convert.osm2gml.debug;

import rescuecore2.misc.gui.ShapeDebugFrame;

import java.util.ArrayList;
import java.util.List;

public class StepVisualizer {
    private final ShapeDebugFrame debug;
    private String title;
    private final List<ShapeLayer<?>> layers;

    private StepVisualizer(final ShapeDebugFrame debug) {
        this.debug = debug;
        this.title = "No title";
        this.layers = new ArrayList<>();
    }

    public static StepVisualizer create(final ShapeDebugFrame debug) {
        return new StepVisualizer(debug);
    }

    public StepVisualizer title(final String title) {
        this.title = title;
        return this;
    }

    public <T> StepVisualizer layer(final ShapeLayer<T> layer) {
        layers.add(layer);
        return this;
    }

    public void show() {
        final List<ShapeDebugFrame.ShapeInfo> shapes = layers.stream()
            .map(ShapeLayer::createShapes)
            .flatMap(List::stream)
            .toList();
        debug.show(title, shapes);
    }
}
