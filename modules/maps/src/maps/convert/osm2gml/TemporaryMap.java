package maps.convert.osm2gml;

import java.awt.geom.Rectangle2D;
import java.util.*;

import maps.osm.OSMMap;

import maps.osm.OSMNode;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.collections.LazyMap;

/**
   This class holds all temporary information during map conversion.
*/
public class TemporaryMap {
    /**
       The threshold for determining if nodes are co-located in metres.
    */
    private static final double NEARBY_THRESHOLD_M = 1;

    private static final double EXACT_THRESHOLD = 1e-10;

    private double threshold;

    private Set<Node> nodes;
    private Set<Edge> edges;
    private Map<Node, Set<Edge>> edgesAtNode;
    private Map<Edge, Set<TemporaryObject>> objectsAtEdge;
    private Set<TemporaryRoad> tempRoads;
    private Set<TemporaryIntersection> tempIntersections;
    private Set<TemporaryBuilding> tempBuildings;
    private Set<TemporaryObject> allObjects;

    private OSMMap osmMap;
    private Collection<OSMIntersectionInfo> osmIntersections;
    private Collection<OSMRoadInfo> osmRoads;
    private Collection<OSMBuildingInfo> osmBuildings;

    private int nextID;

    private Rectangle2D cachedBounds;

    private Map<OSMRoadInfo, OSMIntersectionInfo> roadStarts;
    private Map<OSMRoadInfo, OSMIntersectionInfo> roadEnds;

    /**
       Construct a TemporaryMap.
       @param osmMap The OpenStreetMap data this map is generated from.
    */
    public TemporaryMap(OSMMap osmMap) {
        this.osmMap = osmMap;
        nextID = 0;
        nodes = new HashSet<Node>();
        edges = new HashSet<Edge>();
        threshold = ConvertTools.nearbyThreshold(osmMap, NEARBY_THRESHOLD_M);
        tempRoads = new HashSet<TemporaryRoad>();
        tempIntersections = new HashSet<TemporaryIntersection>();
        tempBuildings = new HashSet<TemporaryBuilding>();
        allObjects = new HashSet<TemporaryObject>();
        edgesAtNode = new LazyMap<Node, Set<Edge>>() {
            @Override
            public Set<Edge> createValue() {
                return new HashSet<Edge>();
            }
        };
        objectsAtEdge = new LazyMap<Edge, Set<TemporaryObject>>() {
            @Override
            public Set<TemporaryObject> createValue() {
                return new HashSet<TemporaryObject>();
            }
        };
    }

    /**
       Get the OSMMap.
       @return The OSMMap.
    */
    public OSMMap getOSMMap() {
        return osmMap;
    }

    /**
     * Set the core OSM graph information (intersections, roads, buildings).
     * This method is the single source of truth for updating the map's graph structure.
     * It automatically rebuilds the internal road-to-intersection mappings to ensure data.
     * @param intersections The new collection of intersections.
     * @param roads         The new collection of roads.
     * @param buildings     The new collection of buildings.
     */
    public void setOSMInfo(Collection<OSMIntersectionInfo> intersections, Collection<OSMRoadInfo> roads, Collection<OSMBuildingInfo> buildings) {
        osmIntersections = new HashSet<>(intersections);
        osmRoads = new HashSet<>(roads);
        osmBuildings = new HashSet<>(buildings);

        Map<OSMNode, OSMIntersectionInfo> nodeToIntersection = new HashMap<>();
        for (OSMIntersectionInfo i : osmIntersections) {
            nodeToIntersection.put(i.getUnderlyingNode(), i);
        }

        Map<OSMRoadInfo, OSMIntersectionInfo> roadStarts = new HashMap<>();
        Map<OSMRoadInfo, OSMIntersectionInfo> roadEnds = new HashMap<>();
        for (OSMRoadInfo road : osmRoads) {
            roadStarts.put(road, nodeToIntersection.get(road.getFrom()));
            roadEnds.put(road, nodeToIntersection.get(road.getTo()));
        }
        this.roadStarts = roadStarts;
        this.roadEnds = roadEnds;
    }

    /**
       Get the OSM intersection info.
       @return The OSM intersection info.
    */
    public Collection<OSMIntersectionInfo> getOSMIntersectionInfo() {
        return Collections.unmodifiableCollection(osmIntersections);
    }

    /**
       Get the OSM road info.
       @return The OSM road info.
    */
    public Collection<OSMRoadInfo> getOSMRoadInfo() {
        return Collections.unmodifiableCollection(osmRoads);
    }

    /**
       Get the OSM building info.
       @return The OSM building info.
    */
    public Collection<OSMBuildingInfo> getOSMBuildingInfo() {
        return Collections.unmodifiableCollection(osmBuildings);
    }

    /**
     * Get the starting intersection for a given road segment.
     * @param road The road segment to look up.
     * @return The starting OSMIntersectionInfo.
     */
    public OSMIntersectionInfo getRoadStartIntersection(OSMRoadInfo road) {
        return roadStarts.get(road);
    }

    /**
     * Get the ending intersection for a given road segment.
     * @param road The road segment to look up.
     * @return The ending OSMIntersectionInfo.
     */
    public OSMIntersectionInfo getRoadEndIntersection(OSMRoadInfo road) {
        return roadEnds.get(road);
    }

    /**
       Add a road.
       @param road The road to add.
    */
    public void addRoad(TemporaryRoad road) {
        tempRoads.add(road);
        addObject(road);
    }

    /**
       Remove a road.
       @param road The road to remove.
    */
    public void removeRoad(TemporaryRoad road) {
        tempRoads.remove(road);
        removeObject(road);
    }

    /**
       Add an intersection.
       @param intersection The intersection to add.
    */
    public void addIntersection(TemporaryIntersection intersection) {
        tempIntersections.add(intersection);
        addObject(intersection);
    }

    /**
       Remove an intersection.
       @param intersection The intersection to remove.
    */
    public void removeIntersection(TemporaryIntersection intersection) {
        tempIntersections.remove(intersection);
        removeObject(intersection);
    }

    /**
       Add a building.
       @param building The building to add.
    */
    public void addBuilding(TemporaryBuilding building) {
        tempBuildings.add(building);
        addObject(building);
    }

    /**
       Remove a building.
       @param building The building to remove.
    */
    public void removeBuilding(TemporaryBuilding building) {
        tempBuildings.remove(building);
        removeObject(building);
    }

    /**
       Add an object.
       @param object The object to add.
    */
    public void addTemporaryObject(TemporaryObject object) {
        if (object instanceof TemporaryRoad) {
            addRoad((TemporaryRoad)object);
        }
        if (object instanceof TemporaryIntersection) {
            addIntersection((TemporaryIntersection)object);
        }
        if (object instanceof TemporaryBuilding) {
            addBuilding((TemporaryBuilding)object);
        }
    }

    /**
       Remove an object.
       @param object The object to remove.
    */
    public void removeTemporaryObject(TemporaryObject object) {
        if (object instanceof TemporaryRoad) {
            removeRoad((TemporaryRoad)object);
        }
        if (object instanceof TemporaryIntersection) {
            removeIntersection((TemporaryIntersection)object);
        }
        if (object instanceof TemporaryBuilding) {
            removeBuilding((TemporaryBuilding)object);
        }
    }

    /**
       Get all roads in the map.
       @return All roads.
    */
    public Collection<TemporaryRoad> getRoads() {
        return new HashSet<TemporaryRoad>(tempRoads);
    }

    /**
       Get all intersections in the map.
       @return All intersections.
    */
    public Collection<TemporaryIntersection> getIntersections() {
        return new HashSet<TemporaryIntersection>(tempIntersections);
    }

    /**
       Get all buildings in the map.
       @return All buildings.
    */
    public Collection<TemporaryBuilding> getBuildings() {
        return new HashSet<TemporaryBuilding>(tempBuildings);
    }

    /**
     * Get all passable shape (roads and intersections) in the map.
     * @return All passable shapes.
     */
    public Collection<TemporaryObject> getAllPassableShapes() {
        final List<TemporaryObject> passable = new ArrayList<>();
        passable.addAll(tempRoads);
        passable.addAll(tempIntersections);
        return passable;
    }

    /**
       Get all objects in the map.
       @return All objects.
    */
    public Collection<TemporaryObject> getAllObjects() {
        return new HashSet<TemporaryObject>(allObjects);
    }

    /**
       Get all nodes in the map.
       @return All nodes.
    */
    public Collection<Node> getAllNodes() {
        return new HashSet<Node>(nodes);
    }

    /**
       Get all edges in the map.
       @return All edges.
    */
    public Collection<Edge> getAllEdges() {
        return new HashSet<Edge>(edges);
    }

    /**
       Get all objects attached to an Edge.
       @param e The Edge.
       @return All attached TemporaryObjects.
    */
    public Set<TemporaryObject> getAttachedObjects(Edge e) {
        return new HashSet<TemporaryObject>(objectsAtEdge.get(e));
    }

    /**
       Get all edges attached to a Node.
       @param n The Node.
       @return All attached edges.
    */
    public Set<Edge> getAttachedEdges(Node n) {
        return new HashSet<Edge>(edgesAtNode.get(n));
    }

    /**
       Set the threshold for deciding if two points are the same. The {@link #isNear(Point2D, Point2D)} method uses this value to check if a new point needs to be registered.
       @param t The new threshold.
    */
    public void setNearbyThreshold(double t) {
        threshold = t;
    }

    /**
       Get the threshold for deciding if two points are the same. The {@link #isNear(Point2D, Point2D)} method uses this value to check if a new point needs to be registered.
       @return The nearby threshold.
    */
    public double getNearbyThreshold() {
        return threshold;
    }

    /**
       Find out if two points are nearby.
       @param point1 The first point.
       @param point2 The second point.
       @return True iff the two points are within the nearby threshold.
    */
    public boolean isNear(Point2D point1, Point2D point2) {
        return isNear(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    /**
       Find out if two points are nearby.
       @param x1 The x coordinate of the first point.
       @param y1 The y coordinate of the first point.
       @param x2 The x coordinate of the second point.
       @param y2 The y coordinate of the second point.
       @return True iff the two points are within the nearby threshold.
    */
    public boolean isNear(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return (dx >= -threshold
                && dx <= threshold
                && dy >= -threshold
                && dy <= threshold);
    }

    /**
       Get a Node near a point. If a Node already exists nearby then it will be returned, otherwise a new Node will be created.
       @param p The node coordinates.
       @return A Node.
    */
    public Node getNode(Point2D p) {
        return getNode(p.getX(), p.getY());
    }

    /**
       Get a Node near a point. If a Node already exists nearby then it will be returned, otherwise a new Node will be created.
       @param x The X coordinate.
       @param y The Y coordinate.
       @return A Node.
    */
    public Node getNode(double x, double y) {
        for (Node next : nodes) {
            if (isNear(x, y, next.getX(), next.getY())) {
                return next;
            }
        }
        return createNode(x, y);
    }

    public Node getNodeExact(final double x, final double y) {
        for (final Node next : nodes) {
            final double dx = next.getX() - x;
            final double dy = next.getY() - y;
            if (Math.abs(dx) <= EXACT_THRESHOLD && Math.abs(dy) <= EXACT_THRESHOLD) {
                return next;
            }
        }
        return createNode(x, y);
    }

    public Node getNodeExact(final Point2D p) {
        return getNodeExact(p.getX(), p.getY());
    }

    /**
       Get an Edge between two nodes. This will return either a new Edge or a shared instance if one already exists.
       @param from The from node.
       @param to The to node.
       @return An Edge.
    */
    public Edge getEdge(Node from, Node to) {
        for (Edge next : edges) {
            if (next.getStart().equals(from) && next.getEnd().equals(to)
                || next.getStart().equals(to) && next.getEnd().equals(from)) {
                return next;
            }
        }
        return createEdge(from, to);
    }

    /**
     * Get a {@code DirectedEdge} between two nodes.
     * Returns {@code null} if the two nodes are the same, as a zero-length edge
     * degenerates to a single node and should be treated as absent.
     * @param from The from node.
     * @param to   The to node.
     * @return     A new {@code DirectedEdge}, or {@code null} if {@code from} and
     *             {@code to} are the same node.
     */
    public DirectedEdge getDirectedEdge(final Node from, final Node to) {
        if (from.equals(to)) return null;
        final Edge e = getEdge(from, to);
        return new DirectedEdge(e, from);
    }

    /**
       Replace an existing edge with a set of new edges.
       @param edge The old edge.
       @param newEdges The new edges.
    */
    public void replaceEdge(Edge edge, Collection<Edge> newEdges) {
        for (TemporaryObject next : getAttachedObjects(edge)) {
            next.replaceEdge(edge, newEdges);
            for (Edge nextEdge : newEdges) {
                objectsAtEdge.get(nextEdge).add(next);
            }
        }
        removeEdge(edge);
    }

    /**
     * Split an edge into chunks.
     * @param edge        The edge to split.
     * @param splitPoints The nodes at which to split the edge.
     * @return The list of replacement edges created by the split, or an empty list if no split occured.
     */
    public List<Edge> splitEdge(final Edge edge, final Collection<Node> splitPoints) {
        final List<Node> sorted = ConvertTools.sortedAlongEdge(edge, splitPoints);

        final List<Edge> replacements = new ArrayList<>();
        Edge current = edge;
        for (final Node n :  sorted) {
            if (n.equals(current.getStart()) || n.equals(current.getEnd())) {
                continue;
            }
            replacements.add(getEdge(current.getStart(), n));
            current = getEdge(n, current.getEnd());
        }
        if (!edge.equals(current)) {
            replacements.add(current);
        }
        if (!replacements.isEmpty()) {
            replaceEdge(edge, replacements);
        }

        invalidateBoundsCache();
        return Collections.unmodifiableList(replacements);
    }

    /**
     * Split an edge into chunks.
     * @param edge        The edge to split.
     * @param splitPoints The nodes at which to split the edge.
     * @return The list of replacement edges created by the split, or an empty list if no split occured.
     * @see #splitEdge(Edge, Collection)
     */
    public List<Edge> splitEdge(Edge edge, Node... splitPoints) {
        return splitEdge(edge, Arrays.asList(splitPoints));
    }

    private Node createNode(double x, double y) {
        Node result = new Node(nextID++, x, y);
        nodes.add(result);

        invalidateBoundsCache();

        return result;
    }

    private Edge createEdge(Node from, Node to) {
        Edge result = new Edge(nextID++, from, to);
        edges.add(result);
        edgesAtNode.get(from).add(result);
        edgesAtNode.get(to).add(result);
        //        Logger.debug("Created edge " + result);
        return result;
    }

    private void addObject(TemporaryObject object) {
        allObjects.add(object);
        for (DirectedEdge next : object.getEdges()) {
            objectsAtEdge.get(next.getEdge()).add(object);
        }
    }

    private void removeEdge(Edge e) {
        edges.remove(e);
        edgesAtNode.get(e.getStart()).remove(e);
        edgesAtNode.get(e.getEnd()).remove(e);
        objectsAtEdge.remove(e);
        //        Logger.debug("Removed edge " + e);
    }

    private void removeObject(TemporaryObject object) {
        allObjects.remove(object);
        for (DirectedEdge next : object.getEdges()) {
            objectsAtEdge.get(next.getEdge()).remove(object);
        }
    }

    /**
     * Returns the bounding rectangle of all nodes in the map.
     *
     * <p>
     * The result is cached after the first call. Call {@link #invalidateBoundsCache()}
     * whenever nodes are added or removed to ensure the cached value is refreshed.
     *
     * <p>
     * Width and height are guaranteed to be at least 1 meter, so that
     * {@link SpatialGrid} never receives an empty rectangle and falls back to
     * its dummy (no-op) mode.
     *
     * @return The bounding rectangle, or an empty {@link Rectangle2D} if the map has no nodes.
     */
    public Rectangle2D getBounds() {
        if (this.cachedBounds != null) {
            return this.cachedBounds;
        }

        final Collection<Node> allNodes = getAllNodes();
        if (allNodes.isEmpty()) {
            this.cachedBounds = new Rectangle2D.Double();
            return this.cachedBounds;
        }

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (final Node node : allNodes) {
            minX = Math.min(minX, node.getX());
            minY = Math.min(minY, node.getY());
            maxX = Math.max(maxX, node.getX());
            maxY = Math.max(maxY, node.getY());
        }

        // Ensure with and height are least 1 meter so that SpatialGrid never
        // receives an empty rectangle and fall back to its dummy (no-op) mode.
        this.cachedBounds = new Rectangle2D.Double(
                minX, minY,
                Math.max(maxX - minX, 1.0),
                Math.max(maxY - minY, 1.0));
        return this.cachedBounds;
    }

    private void invalidateBoundsCache() {
        this.cachedBounds = null;
    }

    /**
     * Rebuild the global edge list and all related mappings from the current set of all TemporaryObjects.
     * This method is computationally expensive and should only be called after major geometric changes
     * that fundamentally alter the shape of objects, such as CleanOverlapsStep.
     */
    public void resynchronizeStateFromObjects() {
        // Clear all existing low-level geometric data.
        nodes.clear();
        edges.clear();
        edgesAtNode.clear();
        objectsAtEdge.clear();

        // Re-populate the data from the high-level TemporaryObjects.
        for (TemporaryObject object : allObjects) {
            for (DirectedEdge dEdge : object.getEdges()) {
                Edge edge = dEdge.getEdge();
                Node start = edge.getStart();
                Node end = edge.getEnd();

                // Add nodes to the global node list
                nodes.add(start);
                nodes.add(end);

                // Add edge to the global edge list
                edges.add(edge);

                // Rebuild the edgeAtNode mapping
                edgesAtNode.get(start).add(edge);
                edgesAtNode.get(end).add(edge);

                // Rebuild the objectsAtEdge mapping
                objectsAtEdge.get(edge).add(object);
            }
        }

        // Invalidate the bounds cache as the node set has changed.
        invalidateBoundsCache();
    }

    /**
     * Check whether an edge exists in the map.
     * @param edge The edge to check.
     * @return True if the edge exists in the map, false otherwise.
     */
    public boolean containsEdge(final Edge edge) {
        return edges.contains(edge);
    }

}
