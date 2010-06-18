package maps.validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import maps.gml.GMLDirectedEdge;
import maps.gml.GMLMap;
import maps.gml.GMLRoad;
import maps.gml.GMLShape;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.linemerge.LineSequencer;

/**
 * Validator to check if the shapes of the map are traversable.
 *
 * For all shaped we check if they can be entered via their entrances.
 *
 * For road we also check if each entrance can reach the other ones via this
 * road.
 *
 */
public class GMLTraversabilityValidator implements MapValidator<GMLMap> {
    private static final double MIN_ROAD_WIDTH = 1.0;
    private static final double SHAPE_PADDING = 0.01;

    @Override
    public Collection<ValidationError> validate(GMLMap map) {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
        for (GMLShape shape : map.getRoads()) {
            ValidationError error = checkTraversability(shape, MIN_ROAD_WIDTH);
            if (error != null) {
                errors.add(error);
            }
        }
        for (GMLShape shape : map.getBuildings()) {
            ValidationError error = checkTraversability(shape, MIN_ROAD_WIDTH);
            if (error != null) {
                errors.add(error);
            }
        }
        return errors;
    }

    /**
     * Check if this shape can be traversed by an agent of width
     * <tt>minWidth</tt>.
     *
     * @param shape
     * @param agentWidth
     * @return
     */
    private ValidationError checkTraversability(GMLShape shape, double minWidth) {
        // To check for traversability, we shrink the non-traversable edges
        // of the shape by the radius of the agent.
        // We then check, if all entrance edges are part of the same part
        // of the resulting polygon

        try {
            Geometry polygon = JTSTools.shapeToPolygon(shape);
            if (!polygon.isValid()) {
                return new ValidationError(shape.getID(), "invalid shape");
            }
            Geometry boundary = impassableLines(shape);
            Geometry buffer = boundary.buffer(((double) minWidth) / 2);
            Geometry result = polygon.difference(buffer);
            // make sure the intersection tests succeed
            result = result.buffer(SHAPE_PADDING);

            Coordinate centroid = JTSTools.pointToCoordinate(shape.getCentroid());

            // Build list of adjacent entrance edges
            List<GMLDirectedEdge> edges = shape.getEdges();
            List<List<GMLDirectedEdge>> entrances = new ArrayList<List<GMLDirectedEdge>>();
            List<GMLDirectedEdge> entrance = new ArrayList<GMLDirectedEdge>();
            for (GMLDirectedEdge e : edges) {
                if (shape.hasNeighbour(e)) {
                    entrance.add(e);
                    //Check if we have a line of sight to the centroid
                    LineString edge = JTSTools.edgeToLine(e);
                    Coordinate edgeCenter = edge.getCentroid().getCoordinate();
                    Coordinate[] coords = new Coordinate[]{centroid, edgeCenter};
                    LineString lineOfSight = JTSTools.getFactory().createLineString(coords);
                    if (lineOfSight.intersects(boundary)) {
                        String message = "Edge " + e.getEdge().getID()
                            + " has no line of sight to shape center.";
                        return new ValidationError(shape.getID(), message);
                    }
                }
                else {
                    if (!entrance.isEmpty()) {
                        entrances.add(entrance);
                    }
                    entrance = new ArrayList<GMLDirectedEdge>();
                }
            }
            if (!entrance.isEmpty()) {
                // Merge first and last sequences if neccessary
                if (shape.hasNeighbour(edges.get(0)) && !entrances.isEmpty()) {
                    entrances.get(0).addAll(entrance);
                }
                else {
                    entrances.add(entrance);
                }
            }

            // Check in which part of the polygon the entrances lie
            GMLDirectedEdge firstEdge = null;
            int firstPolygon = -1;

            for (List<GMLDirectedEdge> etr : entrances) {
                int polyIndex = -1;
                for (GMLDirectedEdge e : etr) {
                    polyIndex = findPolygonPartOfEdge(e, result);
                    if (polyIndex != -1) {
                        break;
                    }
                }

                if (polyIndex == -1) {
                    // Entrance edge no longer in polygon
                    String message = "Edge is too narrow to pass through.";
                    return new ValidationError(etr.get(0).getEdge().getID(),
                            message);
                }
                if (firstEdge == null) {
                    firstEdge = etr.get(0);
                    firstPolygon = polyIndex;
                }
                else if (firstPolygon != polyIndex
                        && (shape instanceof GMLRoad)) {
                    // Only check traversability for roads
                    String message = "Can't reach edge "
                            + firstEdge.getEdge().getID() + " from "
                            + etr.get(0).getEdge().getID();
                    return new ValidationError(shape.getID(), message);
                }

            }

            return null;
        }
        catch (ValidationException e) {
            return e.getError();
        }
    }

    /**
     * Find the index of the subgeometry the given edge is part of. Return -1 if
     * the edge is not contained in the geometry at all.
     * @param edge
     * @param geom
     * @return
     */
    private static int findPolygonPartOfEdge(GMLDirectedEdge edge, Geometry geom) {
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            if (edgePartOfPolygon(edge, geom.getGeometryN(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if an edge is part (i.e intersects) of a given polygon.
     * @param edge
     * @param polygon
     * @return
     */
    private static boolean edgePartOfPolygon(GMLDirectedEdge edge,
            Geometry polygon) {
        // No idea if this works...
        return polygon.intersects(JTSTools.edgeToLine(edge));
    }

    /**
     * Return a LineString or MultiLineString of the impassable edges of a
     * shape.
     * @param shape
     * @return
     */
    private static Geometry impassableLines(GMLShape shape) {
        LineSequencer seq = new LineSequencer();
        for (GMLDirectedEdge e : shape.getEdges()) {
            if (!shape.hasNeighbour(e)) {
                Coordinate[] coord = new Coordinate[2];
                coord[0] = JTSTools.nodeToCoordinate(e.getStartNode());
                coord[1] = JTSTools.nodeToCoordinate(e.getEndNode());
                seq.add(JTSTools.getFactory().createLineString(coord));
            }
        }

        return seq.getSequencedLineStrings();
    }
}
