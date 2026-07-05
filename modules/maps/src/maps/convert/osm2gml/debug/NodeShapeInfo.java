package maps.convert.osm2gml.debug;

import maps.convert.osm2gml.Node;
import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;

public class NodeShapeInfo extends ShapeDebugFrame.Point2DShapeInfo {
    public NodeShapeInfo(Node node, String name, Color colour, boolean square) {
        super(node.getCoordinates(), name, colour, square);
    }
}
