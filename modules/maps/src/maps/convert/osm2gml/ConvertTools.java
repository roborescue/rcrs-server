package maps.convert.osm2gml;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.gui.ShapeDebugFrame;

import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.debug.GMLNodeShapeInfo;
import maps.gml.GMLEdge;
import maps.gml.debug.GMLEdgeShapeInfo;
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

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.*;

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
     * Compute the size of a given distance in meters in latitude/longitude for an OSMMap.
     * @param map    The map to use for calculating the scale.
     * @param metres The distance in metres to convert.
     * @return The equivalent size in degrees.
     */
    public static double sizeOfMeters(OSMMap map, double metres) {
        return sizeOf1Metre(map) * metres;
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
       Find out if an OSMBuilding is defined clockwise or not.
       @param building The OSMBuilding to check.
       @param map The OSM map.
       @return True if the building is defined clockwise, false if anti-clockwise.
    */
    public static boolean isClockwise(OSMBuilding building, OSMMap map) {
        return nearlyEqual(getAnglesSum(building, map), CLOCKWISE_SUM, THRESHOLD);
    }

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

    /**
     * Converts a java.awt.geom.Area into a list of TemporaryRoad objects.
     * This method correctly handles multipart areas but ignores any internal holes.
     * @param area     The Area to convert.
     * @param original The original TemporaryObject, used to determine the correct type for the new objects.
     * @param map      The TemporaryMap needed to create new nodes and edges.
     * @return A list of new TemporaryRoad objects representing the outer contours of the Area.
     */
    public static List<TemporaryObject> areaToTemporaryPassableShapes(Area area, TemporaryObject original, TemporaryMap map) {
        List<TemporaryObject> result = new ArrayList<>();
        PathIterator it = area.getPathIterator(null);
        double[] coords = new double[6];

        while (!it.isDone()) {
            List<DirectedEdge> currentPath = new ArrayList<>();
            Node firstNode = null;
            Node lastNode = null;

            while (!it.isDone()) {
                int type = it.currentSegment(coords);

                if (type == PathIterator.SEG_MOVETO) {
                    // This is the start of a new path. Initialize the start/end nodes.
                    firstNode = map.getNodeExact(coords[0], coords[1]);
                    lastNode = firstNode;
                }
                else if (type == PathIterator.SEG_LINETO) {
                    // Add a segment to the current path.
                    Node nextNode = map.getNodeExact(coords[0], coords[1]);
                    if (lastNode != null && !lastNode.equals(nextNode)) {
                        currentPath.add(map.getDirectedEdge(lastNode, nextNode));
                    }
                    lastNode = nextNode;
                }
                else if (type == PathIterator.SEG_CLOSE) {
                    // This path is now complete. Finalize and add it to the results.
                    if (firstNode != null && lastNode != null && !lastNode.equals(firstNode)) {
                        currentPath.add(map.getDirectedEdge(lastNode, firstNode));
                    }
                    if (2 < currentPath.size()) {
                        if (original instanceof TemporaryRoad) {
                            result.add(new TemporaryRoad(currentPath));
                        } else if (original instanceof TemporaryIntersection) {
                            result.add(new TemporaryIntersection(currentPath));
                        }
                    }

                    // Break the inner loop to start processing the next path (if any).
                    break;
                }
                it.next();
            }
            it.next();
        }
        return result;
    }

    /**
     * Calculate the geometric area of a java.awt.geom.Area object.
     * This method correctly handles multipart areas and ignores holes.
     * @param area The Area to measure.
     * @return The total geometric area.
     */
    public static double getGeometricArea(Area area) {
        if (area == null || area.isEmpty()) return 0.0;

        PathIterator it = area.getPathIterator(null);
        double totalArea = 0;

        while (!it.isDone()) {
            List<Point2D> currentPath = new ArrayList<>();
            double[] coords = new double[6];

            // Extract all points from the current sub-path of the Area
            for (; !it.isDone(); it.next()) {
                int type = it.currentSegment(coords);

                if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                    currentPath.add(new Point2D(coords[0], coords[1]));
                } else if (type == PathIterator.SEG_CLOSE) {
                    // A SEG_CLOSE marks the end of a sub-path.
                    double pathArea = calculatePathSignedArea(currentPath);
                    totalArea += Math.abs(pathArea);

                    // Reset for the next potential shape.
                    currentPath.clear();
                    break;
                }
            }
            it.next();
        }
        return totalArea;
    }

    // Helper method to calculate the signed area of a single path using the Shoelace formula.
    private static double calculatePathSignedArea(List<Point2D> path) {
        if (path.size() < 3) return 0.0;
        double areaSum = 0.0;
        Point2D last = path.getLast();
        for (Point2D current : path) {
            areaSum += (last.getX() * current.getY()) - (current.getX() * last.getY());
            last = current;
        }
        return areaSum / 2.0;
    }

    /**
     * Sort nodes by their position along the given edge.
     * @param edge  The edge defining the direction of sorting.
     * @param nodes The nodes to sort.
     * @return A new list of nodes sorted by t-parameter along the edge.
     */
    public static List<Node> sortedAlongEdge(final Edge edge, final Collection<Node> nodes) {
        final double dx = edge.getLine().getDirection().getX();
        final double dy = edge.getLine().getDirection().getY();
        final double len2 = dx * dx + dy * dy;
        final double ox = edge.getLine().getOrigin().getX();
        final double oy = edge.getLine().getOrigin().getY();

        return nodes.stream()
                .sorted(Comparator.comparingDouble(n -> {
                    // Project the node onto the edge direction to obtain a stable t-parameter.
                    return ((n.getX() - ox) * dx + (n.getY() - oy) * dy) / len2;
                }))
                .toList();
    }

}
