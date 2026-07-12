package maps.convert.osm2gml.debug;

import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;

public class PointShapeInfo extends ShapeDebugFrame.Point2DShapeInfo {
    public PointShapeInfo(Puntal point, String name, Color color, boolean square) {
        super(point.getPoint(), name, color, square);
    }
}
