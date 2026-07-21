package maps.convert.osm2gml;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.stream.Collectors;

import maps.osm.OSMMap;

import maps.osm.OSMNode;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.collections.LazyMap;

/**
   This class holds all temporary information during map conversion.
*/
public class TemporaryMap {
    /** The threshold for determining if nodes are co-located in metres. */
    private static final double NEARBY_THRESHOLD_M = 0.01;
    private final double threshold;

    private final Set<Node> nodes;
    private final Set<Edge> edges;
    private final Map<Node, Set<Edge>> edgesAtNode;
    private final Map<Edge, Set<TemporaryObject>> objectsAtEdge;
    private final Set<TemporaryRoad> tempRoads;
    private final Set<TemporaryIntersection> tempIntersections;
    private final Set<TemporaryBuilding> tempBuildings;
    private final Set<TemporaryObject> allObjects;

    private final OSMMap osmMap;
    private final Set<OSMIntersectionInfo> osmIntersections;
    private final Set<OSMRoadInfo> osmRoads;
    private final Set<OSMBuildingInfo> osmBuildings;

    private int nextID;

    private Rectangle2D cachedBounds;

    private final double gridSpacing; // size of 1 meter in map coordinates
    private final double gridOriginX; // map center X (grid anchor)
    private final double gridOriginY; // map center Y (grid anchor)
    private final Map<Long, Node> nodeGrid;

    /**
       Construct a TemporaryMap.
       @param osmMap The OpenStreetMap data this map is generated from.
    */
    public TemporaryMap(OSMMap osmMap) {
        this.osmMap           = osmMap;
        this.osmIntersections = new LinkedHashSet<>();
        this.osmRoads         = new LinkedHashSet<>();
        this.osmBuildings     = new LinkedHashSet<>();

        nextID = 0;
        nodes = new LinkedHashSet<>();
        edges = new LinkedHashSet<>();
        threshold = ConvertTools.nearbyThreshold(osmMap, NEARBY_THRESHOLD_M);
        tempRoads = new LinkedHashSet<>();
        tempIntersections = new LinkedHashSet<>();
        tempBuildings = new LinkedHashSet<>();
        allObjects = new LinkedHashSet<>();
        edgesAtNode = new LazyMap<>() {
            @Override
            public Set<Edge> createValue() {
                return new LinkedHashSet<>();
            }
        };
        objectsAtEdge = new LazyMap<>() {
            @Override
            public Set<TemporaryObject> createValue() {
                return new LinkedHashSet<>();
            }
        };

        this.gridSpacing = ConvertTools.sizeOf1Metre(osmMap);
        this.gridOriginX = osmMap.getCenterLatitude();
        this.gridOriginY = osmMap.getCenterLongitude();
        this.nodeGrid    = new LinkedHashMap<>();
    }

    /**
       Get the OSMMap.
       @return The OSMMap.
    */
    public OSMMap getOSMMap() {
        return osmMap;
    }

    public void addOSMIntersection(final OSMIntersectionInfo intersection) {
        osmIntersections.add(intersection);
    }

    public void removeOSMIntersection(final OSMIntersectionInfo intersection) {
        osmIntersections.remove(intersection);
    }

    public void addOSMRoad(final OSMRoadInfo road) {
        final OSMIntersectionInfo from = getOSMIntersection(road.getFrom());
        final OSMIntersectionInfo to = getOSMIntersection(road.getTo());
        osmRoads.add(road);
        from.addRoadSegment(road);
        to.addRoadSegment(road);
    }

    public void removeOSMRoad(final OSMRoadInfo road) {
        final OSMIntersectionInfo from = getOSMIntersection(road.getFrom());
        final OSMIntersectionInfo to = getOSMIntersection(road.getTo());
        osmRoads.remove(road);
        from.removeRoadSegment(road);
        to.removeRoadSegment(road);
    }

    public void addOSMBuilding(final OSMBuildingInfo osmBuildingInfo) {
        osmBuildings.add(osmBuildingInfo);
    }

    public Set<OSMRoadInfo> getConnectedOSMRoads(final OSMIntersectionInfo intersection) {
        return getOSMRoadsContaining(intersection.getNode());
    }

    public Set<OSMRoadInfo> getOSMRoadsContaining(final OSMNode node) {
        return osmRoads.stream()
                .filter(road -> road.contains(node))
                .collect(Collectors.toUnmodifiableSet());
    }

    public OSMIntersectionInfo getOSMIntersection(final OSMNode node) {
        return osmIntersections.stream()
                .filter(intersection -> intersection.getNode().equals(node))
                .findFirst()
                .orElseThrow();
    }

    public int getOSMIntersectionDegree(final OSMIntersectionInfo intersection) {
        return getConnectedOSMRoads(intersection).size();
    }

    /**
       Get the OSM intersection info.
       @return The OSM intersection info.
    */
    public Collection<OSMIntersectionInfo> getOSMIntersections() {
        return Collections.unmodifiableCollection(osmIntersections);
    }

    /**
       Get the OSM road info.
       @return The OSM road info.
    */
    public Collection<OSMRoadInfo> getOSMRoads() {
        return Collections.unmodifiableCollection(osmRoads);
    }

    /**
       Get the OSM building info.
       @return The OSM building info.
    */
    public Collection<OSMBuildingInfo> getOSMBuildings() {
        return Collections.unmodifiableCollection(osmBuildings);
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
        return Collections.unmodifiableSet(tempRoads);
    }

    /**
       Get all intersections in the map.
       @return All intersections.
    */
    public Collection<TemporaryIntersection> getIntersections() {
        return Collections.unmodifiableSet(tempIntersections);
    }

    /**
       Get all buildings in the map.
       @return All buildings.
    */
    public Collection<TemporaryBuilding> getBuildings() {
        return Collections.unmodifiableSet(tempBuildings);
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
        return Collections.unmodifiableSet(allObjects);
    }

    /**
       Get all nodes in the map.
       @return All nodes.
    */
    public Collection<Node> getAllNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    /**
       Get all edges in the map.
       @return All edges.
    */
    public Collection<Edge> getAllEdges() {
        return Collections.unmodifiableSet(edges);
    }

    /**
     * Get all objects attached to the specified edge.
     * @param e The edge.
     * @return An unmodifiable set of objects attached to the edge.
     */
    public Set<TemporaryObject> getAttachedObjects(Edge e) {
        return Collections.unmodifiableSet(objectsAtEdge.get(e));
    }

    /**
     * Get all objects attached to the specified directed edge.
     * @param e The directed edge.
     * @return An unmodifiable set of objects attached to the underlying edge.
     */
    public Set<TemporaryObject> getAttachedObjects(DirectedEdge e) {
        return getAttachedObjects(e.getEdge());
    }

    /**
       Get all edges attached to a Node.
       @param n The Node.
       @return All attached edges.
    */
    public Set<Edge> getAttachedEdges(Node n) {
        return Collections.unmodifiableSet(edgesAtNode.get(n));
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
     * Returns the node at the specified location.
     *
     * @param point the node location
     * @return the node at the specified location
     * @see #getNode(double, double)
     */
    public Node getNode(Point2D point) {
        return getNode(point.getX(), point.getY());
    }

    /**
     * Returns the node at the specified location, creating it if necessary.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the existing or newly created node
     */
    public Node getNode(double x, double y) {
        long ix = Math.round((x - gridOriginX) / gridSpacing);
        long iy = Math.round((y - gridOriginY) / gridSpacing);
        long key = gridKey(ix, iy);

        // Return the existing node for this grid cell if one exists.
        Node existing = nodeGrid.get(key);
        if (existing != null) return existing;

        // No node exists yet; create one at the grid-center coordinates.
       double cx = gridOriginX + ix * gridSpacing;
       double cy = gridOriginY + iy * gridSpacing;
       return createAndRegisterNode(cx, cy);
    }

    /**
     * Returns whether this map contains the specified node.
     *
     * @param node the node to check
     * @return {@code true} if this map contains the specified node;
     *         {@code false} otherwise
     */
    @SuppressWarnings("unused")
    public boolean containsNode(Node node) {
        return containsNode(node.getX(), node.getY());
    }

    /**
     * Returns whether this map contains a node at the specified location.
     *
     * @param point the location to check
     * @return {@code true} if this map contains a node at the specified location;
     *         {@code false} otherwise
     */
    public boolean containsNode(Point2D point) {
        return containsNode(point.getX(), point.getY());
    }

    /**
     * Returns whether this map contains a node at the specified location.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return {@code true} if this map contains a node at the specified location;
     *         {@code false} otherwise
     */
    public boolean containsNode(double x, double y) {
        long ix = Math.round((x - gridOriginX) / gridSpacing);
        long iy = Math.round((y - gridOriginY) / gridSpacing);
        long key = gridKey(ix, iy);

        return nodeGrid.containsKey(key);
    }

    // Encode a 2D grid index as a single long key.
    private static long gridKey(final long ix, final long iy) {
        return (ix << 32) | (iy & 0xFFFFFFFFL);
    }

    // Create a new node at (x, y), register it in both the node set and the grid index.
    private Node createAndRegisterNode(final double x, final double y) {
        final Node node = new Node(nextID++, x, y);
        nodes.add(node);

        final long ix = Math.round((x - gridOriginX) / gridSpacing);
        final long iy = Math.round((y - gridOriginY) / gridSpacing);
        final long key = gridKey(ix, iy);
        nodeGrid.put(key, node);

        invalidateBoundsCache();

        return node;
    }

    /**
     * Get the edge connecting two nodes, creating it if necessary.
     * @param from One end of the edge.
     * @param to   The other end of the edge.
     * @return The existing or newly created edge.
     */
    public Edge getEdge(final Node from, final Node to) {
        for (final Edge next : edges) {
            if (next.getStart().equals(from) && next.getEnd().equals(to)
             || next.getStart().equals(to)   && next.getEnd().equals(from)) {
                return next;
            }
        }
        return createEdge(from, to);
    }

    /**
     * Returns whether this map contains the specified edge.
     *
     * @param edge the edge to check
     * @return {@code true} if this map contains the specified edge;
     *         {@code false} otherwise
     */
    public boolean containsEdge(Edge edge) {
        return edges.contains(edge);
    }

    /**
     * Get a directed edge from one node to another.
     * @param from The start node.
     * @param to   The end node.
     * @return     The directed edge, or {@code null} if both nodes are the same.
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
     * @return The list of replacement edges created by the split, or an empty list if no split occurred.
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
     * @return The list of replacement edges created by the split, or an empty list if no split occurred.
     * @see #splitEdge(Edge, Collection)
     */
    public List<Edge> splitEdge(Edge edge, Node... splitPoints) {
        return splitEdge(edge, Arrays.asList(splitPoints));
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
}
