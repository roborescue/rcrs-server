package maps.convert.osm2gml.debug;

import maps.convert.osm2gml.Edge;
import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class EdgeLayer extends ShapeLayer<Edge> {
    private Color color = Color.BLACK;

    private EdgeLayer(final Collection<Edge> edges) {
        super(edges);
    }

    public static EdgeLayer of(final Collection<Edge> edges) {
        return new EdgeLayer(edges);
    }

    public EdgeLayer name(final String name) {
        this.name = name;
        return this;
    }

    public EdgeLayer color(final Color color) {
        this.color = color;
        return this;
    }

    @Override
    public List<ShapeDebugFrame.ShapeInfo> createShapes() {
        return objects.stream().map(this::createShape).toList();
    }

    private ShapeDebugFrame.ShapeInfo createShape(final Edge edge) {
        return new EdgeShapeInfo(edge, name, color, false, false);
    }
}
