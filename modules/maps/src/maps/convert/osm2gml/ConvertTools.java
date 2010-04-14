package maps.convert.osm2gml;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.gui.ShapeDebugFrame;
//import rescuecore2.log.Logger;

import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.debug.GMLNodeShapeInfo;
import maps.gml.GMLEdge;
import maps.gml.debug.GMLEdgeShapeInfo;
//import maps.gml.GMLFace;
//import maps.gml.debug.GMLFaceShapeInfo;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLObject;
import maps.gml.GMLRoad;
import maps.gml.GMLBuilding;
import maps.gml.GMLSpace;
import maps.gml.GMLShape;
import maps.gml.debug.GMLShapeInfo;
import maps.osm.OSMMap;
import maps.osm.OSMNode;
import maps.osm.OSMBuilding;
import maps.MapTools;

import java.util.Set;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import java.awt.Color;

/**
   Useful tools for converting OSM to GML.
 */
public final class ConvertTools {
    private static final Color BACKGROUND_BUILDING_COLOUR = new Color(0, 255, 0, 32); // Transparent lime
    private static final Color BACKGROUND_INTERSECTION_COLOUR = new Color(192, 192, 192, 32); // Transparent silver
    private static final Color BACKGROUND_ROAD_COLOUR = new Color(128, 128, 128, 32); // Transparent gray
    private static final Color BACKGROUND_SPACE_COLOUR = new Color(0, 128, 0, 32); // Transparent green

    private static final double CLOCKWISE_SUM = -360;
    private static final double THRESHOLD = 0.0001;

    private ConvertTools() {}

    /**
       Compute the size of one metre in latitude/longitude for an OSMMap.
       @param map The map to look up.
       @return The size of one metre on the given map.
    */
    public static double sizeOf1Metre(OSMMap map) {
        return MapTools.sizeOf1Metre(map.getCentreLatitude(), map.getCentreLongitude());
    }

    /**
       Compute the nearby-node threshold for an OSMMap in degrees.
       @param map The map to look up.
       @param thresholdM The desired threshold in meters.
       @return The size of the nearby-node threshold for the map in degrees.
    */
    public static double nearbyThreshold(OSMMap map, double thresholdM) {
        return sizeOf1Metre(map) * thresholdM;
    }

    /**
       Convert a GMLEdge to a Line2D.
       @param edge The edge to convert.
       @return A new Line2D.
    */
    public static Line2D gmlEdgeToLine(GMLEdge edge) {
        GMLNode start = edge.getStart();
        GMLNode end = edge.getEnd();
        Point2D origin = new Point2D(start.getX(), start.getY());
        Point2D endPoint = new Point2D(end.getX(), end.getY());
        return new Line2D(origin, endPoint);
    }

    /**
       Convert a GMLEdge to a Line2D.
       @param edge The edge to convert.
       @param start The node to start from. This must be one of the endpoints of the edge.
       @return A new Line2D.
    */
    public static Line2D gmlEdgeToLine(GMLEdge edge, GMLNode start) {
        if (!start.equals(edge.getStart()) && !start.equals(edge.getEnd())) {
            throw new IllegalArgumentException("'start' must be one of the endpoints of 'edge'");
        }
        GMLNode end = start.equals(edge.getStart()) ? edge.getEnd() : edge.getStart();
        Point2D origin = new Point2D(start.getX(), start.getY());
        Point2D endPoint = new Point2D(end.getX(), end.getY());
        return new Line2D(origin, endPoint);
    }

    /**
       Convert a GMLDirectedEdge to a Line2D.
       @param edge The edge to convert.
       @return A new Line2D.
    */
    public static Line2D gmlDirectedEdgeToLine(GMLDirectedEdge edge) {
        GMLNode start = edge.getStartNode();
        GMLNode end = edge.getEndNode();
        Point2D origin = new Point2D(start.getX(), start.getY());
        Point2D endPoint = new Point2D(end.getX(), end.getY());
        return new Line2D(origin, endPoint);
    }

    /**
       Find the "leftmost" turn in a particular direction, i.e. the one with the highest left angle, or the lowest right angle if there are no left turns possible.
       @param from The edge we're turning from.
       @param candidates The set of edges we could turn into.
       @return The leftmost turn.
    */
    public static Edge findLeftTurn(DirectedEdge from, Set<? extends Edge> candidates) {
        return findBestTurn(from, candidates, true);
    }

    /**
       Find the "rightmost" turn in a particular direction, i.e. the one with the highest right angle, or the lowest left angle if there are no right turns possible.
       @param from The edge we're turning from.
       @param candidates The set of edges we could turn into.
       @return The rightmost turn.
    */
    public static Edge findRightTurn(DirectedEdge from, Set<? extends Edge> candidates) {
        return findBestTurn(from, candidates, false);
    }

    /**
       Find the "best" turn in a particular direction. If left turns are preferred then the "best" turn is the one with the highest left angle, or the lowest right angle. For right turns, the "best" is the one with the highest right angle or the lowest left angle.
       @param from The edge we're turning from.
       @param candidates The set of edges we could turn into.
       @param preferLeft Whether to prefer left turns or not.
       @return The best turn.
    */
    public static Edge findBestTurn(DirectedEdge from, Set<? extends Edge> candidates, boolean preferLeft) {
        Edge mostRight = null;
        Edge mostLeft = null;
        Edge leastRight = null;
        Edge leastLeft = null;
        double mostRightAngle = 0;
        double mostLeftAngle = 0;
        double leastRightAngle = 0;
        double leastLeftAngle = 0;
        Line2D fromLine = from.getLine();
        for (Edge next : candidates) {
            if (next.equals(from.getEdge())) {
                continue;
            }
            Line2D nextLine = next.getLine();
            if (!next.getStart().equals(from.getEndNode())) {
                nextLine = new Line2D(nextLine.getEndPoint(), nextLine.getOrigin());
            }
            if (GeometryTools2D.isRightTurn(fromLine, nextLine)) {
                double angle = GeometryTools2D.getRightAngleBetweenLines(fromLine, nextLine);
                if (mostRight == null || angle > mostRightAngle) {
                    mostRight = next;
                    mostRightAngle = angle;
                }
                if (leastRight == null || angle < leastRightAngle) {
                    leastRight = next;
                    leastRightAngle = angle;
                }
            }
            else {
                double angle = GeometryTools2D.getLeftAngleBetweenLines(fromLine, nextLine);
                if (mostLeft == null || angle > mostLeftAngle) {
                    mostLeft = next;
                    mostLeftAngle = angle;
                }
                if (leastLeft == null || angle < leastLeftAngle) {
                    leastLeft = next;
                    leastLeftAngle = angle;
                }
            }
        }
        if (preferLeft) {
            if (mostLeft != null) {
                return mostLeft;
            }
            return leastRight;
        }
        else {
            if (mostRight != null) {
                return mostRight;
            }
            return leastLeft;
        }
    }

    /**
       Create ShapeInfo objects for all GMLShapes in a map.
       @param map The map to debug.
       @return A list of ShapeInfo objects.
    */
    public static List<ShapeDebugFrame.ShapeInfo> getAllDebugShapes(GMLMap map) {
        return createGMLShapeDebug(map.getAllShapes());
    }

    /**
       Create ShapeInfo objects for all TemporaryObjects in a map.
       @param map The map to debug.
       @return A list of ShapeInfo objects.
    */
    public static List<ShapeDebugFrame.ShapeInfo> getAllDebugShapes(TemporaryMap map) {
        return createTemporaryObjectDebug(map.getAllObjects());
    }

    /**
       Create ShapeInfo objects for a set of GMLShapes.
       @param objects The objects to debug.
       @return A list of ShapeInfo objects.
    */
    public static List<ShapeDebugFrame.ShapeInfo> createGMLShapeDebug(GMLShape... objects) {
        return createGMLShapeDebug(Arrays.asList(objects));
    }

    /**
       Create ShapeInfo objects for a set of GMLShapes.
       @param objects The objects to debug.
       @return A list of ShapeInfo objects.
    */
    public static List<ShapeDebugFrame.ShapeInfo> createGMLShapeDebug(Collection<? extends GMLShape> objects) {
        List<ShapeDebugFrame.ShapeInfo> allShapes = new ArrayList<ShapeDebugFrame.ShapeInfo>();
        for (GMLShape next : objects) {
            Color c = Constants.TRANSPARENT_RED;
            String name = "Unknown";
            if (next instanceof GMLRoad) {
                c = BACKGROUND_ROAD_COLOUR;
                name = "Roads";
            }
            if (next instanceof GMLBuilding) {
                c = BACKGROUND_BUILDING_COLOUR;
                name = "Buildings";
            }
            if (next instanceof GMLSpace) {
                c = BACKGROUND_SPACE_COLOUR;
                name = "Spaces";
            }
            allShapes.add(new GMLShapeInfo(next, name, Color.BLACK, c));
        }
        return allShapes;
    }

    /**
       Create ShapeInfo objects for a set of GMLObjects.
       @param objects The objects to debug.
       @return A list of ShapeInfo objects.
    */
    public static List<ShapeDebugFrame.ShapeInfo> createGMLObjectDebug(GMLObject... objects) {
        return createGMLObjectDebug(Arrays.asList(objects));
    }

    /**
       Create ShapeInfo objects for a set of GMLObjects.
       @param objects The objects to debug.
       @return A list of ShapeInfo objects.
    */
    public static List<ShapeDebugFrame.ShapeInfo> createGMLObjectDebug(Collection<? extends GMLObject> objects) {
        List<ShapeDebugFrame.ShapeInfo> allShapes = new ArrayList<ShapeDebugFrame.ShapeInfo>();
        for (GMLObject object : objects) {
            if (object instanceof GMLNode) {
                allShapes.add(new GMLNodeShapeInfo((GMLNode)object, "Nodes", Constants.BLACK, true));
            }
            if (object instanceof GMLEdge) {
                allShapes.add(new GMLEdgeShapeInfo((GMLEdge)object, "Edges", Constants.BLACK, false));
            }
            /*
            if (object instanceof GMLFace) {
                GMLFace face = (GMLFace)object;
                Color c = Constants.TRANSPARENT_RED;
                String name = "Unknown";
                allShapes.add(new GMLFaceShapeInfo(face, "Faces", Constants.BLACK, Constants.TRANSPARENT_AQUA, false));
            }
            */
        }
        return allShapes;
    }

    /**
       Create ShapeInfo objects for a set of TemporaryObjects.
       @param objects The objects to debug.
       @return A list of ShapeInfo objects.
    */
    public static List<ShapeDebugFrame.ShapeInfo> createTemporaryObjectDebug(TemporaryObject... objects) {
        return createTemporaryObjectDebug(Arrays.asList(objects));
    }

    /**
       Create ShapeInfo objects for a set of TemporaryObjects.
       @param objects The objects to debug.
       @return A list of ShapeInfo objects.
    */
    public static List<ShapeDebugFrame.ShapeInfo> createTemporaryObjectDebug(Collection<? extends TemporaryObject> objects) {
        List<ShapeDebugFrame.ShapeInfo> allShapes = new ArrayList<ShapeDebugFrame.ShapeInfo>();
        for (TemporaryObject next : objects) {
            Color c = Constants.TRANSPARENT_RED;
            String name = "Unknown";
            if (next instanceof TemporaryRoad) {
                c = BACKGROUND_ROAD_COLOUR;
                name = "Roads";
            }
            if (next instanceof TemporaryBuilding) {
                c = BACKGROUND_BUILDING_COLOUR;
                name = "Buildings";
            }
            if (next instanceof TemporaryIntersection) {
                c = BACKGROUND_INTERSECTION_COLOUR;
                name = "Intersections";
            }
            allShapes.add(new TemporaryObjectInfo(next, name, Color.BLACK, c));
        }
        return allShapes;
    }

    /**
       Is a number approximately equal to another number?
       @param n The number to test.
       @param expected The expected value.
       @param threshold The threshold.
       @return If n is within [expected - threshold, expected + threshold].
    */
    public static boolean nearlyEqual(double n, double expected, double threshold) {
        return (n >= expected - threshold
                && n <= expected + threshold);
    }

    /**
       Find out if a set of GMLDirectedEdges is convex.
       @param edges The set of edges to test.
       @return True iff the face is convex.
    */
    public static boolean isConvex(List<DirectedEdge> edges) {
        Iterator<DirectedEdge> it = edges.iterator();
        Line2D first = it.next().getLine();
        Line2D a = first;
        Line2D b = it.next().getLine();
        boolean rightTurn = GeometryTools2D.isRightTurn(a, b);
        while (it.hasNext()) {
            a = b;
            b = it.next().getLine();
            if (rightTurn != GeometryTools2D.isRightTurn(a, b)) {
                return false;
            }
        }
        if (rightTurn != GeometryTools2D.isRightTurn(b, first)) {
            return false;
        }
        return true;
    }

    /**
       Sum the angles of all turns in a GMLFace.
       @param face The face to check.
       @return The sum of angles in the face.
    */
    /*
    public static double getAnglesSum(GMLFace face) {
        double sum = 0;
        Iterator<GMLDirectedEdge> it = face.getEdges().iterator();
        GMLDirectedEdge first = it.next();
        GMLDirectedEdge a = first;
        while (it.hasNext()) {
            GMLDirectedEdge b = it.next();
            double d = getAngle(a, b);
            if (!Double.isNaN(d)) {
                sum += d;
            }
            a = b;
        }
        double d = getAngle(a, first);
        if (!Double.isNaN(d)) {
            sum += d;
        }
        return sum;
    }
    */

    /**
       Sum the angles of all turns in an OSMBuilding.
       @param building The building to check.
       @param map The OSMMap the building is part of.
       @return The sum of angles in the building.
    */
    public static double getAnglesSum(OSMBuilding building, OSMMap map) {
        double sum = 0;
        Iterator<Long> it = building.getNodeIDs().iterator();
        long first = it.next();
        long second = it.next();
        long a = first;
        long b = second;
        while (it.hasNext()) {
            long c = it.next();
            double d = getAngle(a, b, c, map);
            //            Logger.debug("Angle from " + a + ":" + b + ":" + c + " = " + d);
            if (!Double.isNaN(d)) {
                sum += d;
            }
            a = b;
            b = c;
        }
        double d = getAngle(a, first, second, map);
        //        Logger.debug("Angle from " + a + ":" + first + ":" + second + " = " + d);
        if (!Double.isNaN(d)) {
            sum += d;
        }
        return sum;
    }

    /**
       Find out if a GMLFace is defined clockwise or not.
       @param face The GMLFace to check.
       @return True if the face is defined clockwise, false if anti-clockwise.
    */
    /*
    public static boolean isClockwise(GMLFace face) {
        return nearlyEqual(getAnglesSum(face), CLOCKWISE_SUM, THRESHOLD);
    }
    */

    /**
       Find out if an OSMBuilding is defined clockwise or not.
       @param building The OSMBuilding to check.
       @param map The OSM map.
       @return True if the building is defined clockwise, false if anti-clockwise.
    */
    public static boolean isClockwise(OSMBuilding building, OSMMap map) {
        return nearlyEqual(getAnglesSum(building, map), CLOCKWISE_SUM, THRESHOLD);
    }

    /*
    private static double getAngle(GMLDirectedEdge a, GMLDirectedEdge b) {
        Vector2D v1 = new Vector2D(a.getEndNode().getX() - a.getStartNode().getX(), a.getEndNode().getY() - a.getStartNode().getY());
        Vector2D v2 = new Vector2D(b.getEndNode().getX() - b.getStartNode().getX(), b.getEndNode().getY() - b.getStartNode().getY());
        double d = GeometryTools2D.getAngleBetweenVectors(v1, v2);
        if (GeometryTools2D.isRightTurn(v1, v2)) {
            return -d;
        }
        return d;
    }
    */

    private static double getAngle(long first, long second, long third, OSMMap map) {
        OSMNode n1 = map.getNode(first);
        OSMNode n2 = map.getNode(second);
        OSMNode n3 = map.getNode(third);
        Vector2D v1 = new Vector2D(n2.getLongitude() - n1.getLongitude(), n2.getLatitude() - n1.getLatitude());
        Vector2D v2 = new Vector2D(n3.getLongitude() - n2.getLongitude(), n3.getLatitude() - n2.getLatitude());
        double d = GeometryTools2D.getAngleBetweenVectors(v1, v2);
        if (GeometryTools2D.isRightTurn(v1, v2)) {
            return -d;
        }
        return d;
    }
}
