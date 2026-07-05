package maps.convert.osm2gml.debug;

import maps.convert.osm2gml.Node;
import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class NodeLayer extends ShapeLayer<Node> {
    private Color color = Color.BLACK;

    private NodeLayer(final Collection<Node> objects) {
        super(objects);
    }

    public static NodeLayer of(final Collection<Node> objects) {
        return new NodeLayer(objects);
    }

    public NodeLayer name(final String name) {
        this.name = name;
        return this;
    }

    public NodeLayer color(final Color color) {
        this.color = color;
        return this;
    }

    @Override
    public List<ShapeDebugFrame.ShapeInfo> createShapes() {
        return objects.stream().map(this::createShape).toList();
    }

    private ShapeDebugFrame.ShapeInfo createShape(Node node) {
        return new NodeShapeInfo(node, name, color, true);
    }
}
