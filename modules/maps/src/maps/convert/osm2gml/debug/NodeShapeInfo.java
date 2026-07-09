package maps.convert.osm2gml.debug;

import maps.convert.osm2gml.Node;
import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;

/**
 * A {@link rescuecore2.misc.gui.ShapeDebugFrame.Point2DShapeInfo} for debugging a {@link Node}
 */
public class NodeShapeInfo extends ShapeDebugFrame.Point2DShapeInfo {
    /**
     * Construct a new {@code NodeShapeInfo} object.
     * @param node   The node to display.
     * @param name   The name of the node.
     * @param colour The color of the node.
     * @param square Whether to draw as a square or a cross. If {@code false} then a cross will be drawn.
     */
    public NodeShapeInfo(Node node, String name, Color colour, boolean square) {
        super(node.getCoordinates(), name, colour, square);
    }
}
