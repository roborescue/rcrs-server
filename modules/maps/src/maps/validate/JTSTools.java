package maps.validate;

import maps.gml.GMLDirectedEdge;
import maps.gml.GMLNode;
import maps.gml.GMLShape;
import rescuecore2.misc.geometry.Point2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.operation.linemerge.LineSequencer;
import com.vividsolutions.jts.util.AssertionFailedException;

/**
 * This class provides some conversion functions from GML map classes to JTS
 * Geometry classes.
 */
public final class JTSTools {
    private static GeometryFactory geomFactory = new GeometryFactory();

    private JTSTools() {
    };

    /**
     * Create a LineString from a GMLDirectedEdge.
     * @param edge
     *            The edge to convert.
     * @return LineString or MultLineString.
     */
    public static LineString edgeToLine(GMLDirectedEdge edge) {
        Coordinate[] coord = new Coordinate[2];
        coord[0] = nodeToCoordinate(edge.getStartNode());
        coord[1] = nodeToCoordinate(edge.getEndNode());
        return geomFactory.createLineString(coord);
    }

    /**
     * Create a JTS Polygon form a GMLShape.
     * @param shape
     *            The shape to convert.
     * @return Polygon geometry.
     * @throws ValidationException
     */
    public static Geometry shapeToPolygon(GMLShape shape)
            throws ValidationException {
        LineSequencer seq = new LineSequencer();
        for (GMLDirectedEdge e : shape.getEdges()) {
            Coordinate[] coord = new Coordinate[2];
            coord[0] = nodeToCoordinate(e.getStartNode());
            coord[1] = nodeToCoordinate(e.getEndNode());
            if (coord[0].equals(coord[1])) {
                throw new ValidationException(e.getEdge().getID(),
                        "Zero length edge.");
            }
            seq.add(geomFactory.createLineString(coord));
        }

        try {
            if (!seq.isSequenceable()) {
                throw new ValidationException(shape.getID(),
                "Outline is not a single line.");
            }
        }
        catch (AssertionFailedException e) {
            throw new ValidationException(shape.getID(),
                    "Could not get outline: " + e.getMessage());
        }
        Geometry line = seq.getSequencedLineStrings();

        CoordinateList coord = new CoordinateList(line.getCoordinates());
        coord.closeRing();

        // CHECKSTYLE:OFF:MagicNumber
        if (coord.size() < 4) {
            // CHECKSTYLE:ON:MagicNumber
            throw new ValidationException(shape.getID(), "Degenerate Shape");
        }

        Geometry ring = geomFactory.createLinearRing(coord.toCoordinateArray());
        return geomFactory.createPolygon((LinearRing) ring, null);
    }

    /**
     * Create a Coordinate from a GMLNode.
     * @param node
     *            Node to convert.
     * @return Coordinate object.
     */
    public static Coordinate nodeToCoordinate(GMLNode node) {
        return new Coordinate(node.getX(), node.getY());
    }

    /**
     * Create a Coordinate from a Point2D.
     * @param point
     *            Point to convert.
     * @return Coordinate object.
     */
    public static Coordinate pointToCoordinate(Point2D point) {
        return new Coordinate(point.getX(), point.getY());
    }

    /**
     * Get the default GeometryFactory.
     * @return The default GeometryFactory.
     */
    public static GeometryFactory getFactory() {
        return geomFactory;
    }

}
