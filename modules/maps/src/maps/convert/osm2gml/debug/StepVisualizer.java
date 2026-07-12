package maps.convert.osm2gml.debug;

import rescuecore2.misc.gui.ShapeDebugFrame;

import java.util.ArrayList;
import java.util.List;

public class StepVisualizer {
    private final ShapeDebugFrame debug;
    private String title;
    private final List<ShapeLayer<?>> layers;
    private final List<ShapeLayer<?>> backgroundLayers;

    private StepVisualizer(final ShapeDebugFrame debug) {
        this.debug = debug;
        this.title = "No title";
        this.layers = new ArrayList<>();
        this.backgroundLayers = new ArrayList<>();
    }

    public static StepVisualizer create(final ShapeDebugFrame debug) {
        return new StepVisualizer(debug);
    }

    public StepVisualizer title(final String title) {
        this.title = title;
        return this;
    }

    public StepVisualizer layer(final ShapeLayer<?> layer) {
        layers.add(layer);
        return this;
    }

    @SuppressWarnings("unused")
    public StepVisualizer backgroundLayer(final ShapeLayer<?> layer) {
        backgroundLayers.add(layer);
        return this;
    }

    public void show() {
        if (!backgroundLayers.isEmpty()) debug.setBackground(flatten(backgroundLayers));
        debug.show(title, flatten(layers));
    }

    private List<ShapeDebugFrame.ShapeInfo> flatten(final List<ShapeLayer<?>> src) {
        return src.stream().map(ShapeLayer::createShapes).flatMap(List::stream).toList();
    }
}
