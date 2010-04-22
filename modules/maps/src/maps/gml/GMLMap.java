package maps.gml;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import maps.CoordinateConversion;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;

/**
   A GML map. All coordinates are specified in m.
*/
public class GMLMap {
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private boolean boundsKnown;

    private Map<Integer, GMLNode> nodes;
    private Map<Integer, GMLEdge> edges;
    private Map<Integer, GMLBuilding> buildings;
    private Map<Integer, GMLRoad> roads;
    private Map<Integer, GMLSpace> spaces;
    private Set<GMLShape> allShapes;
    private Set<GMLObject> allObjects;

    private int nextID;

    /**
       Construct an empty GML map.
     */
    public GMLMap() {
        nodes = new HashMap<Integer, GMLNode>();
        edges = new HashMap<Integer, GMLEdge>();
        buildings = new HashMap<Integer, GMLBuilding>();
        roads = new HashMap<Integer, GMLRoad>();
        spaces = new HashMap<Integer, GMLSpace>();
        allShapes = new HashSet<GMLShape>();
        allObjects = new HashSet<GMLObject>();
        boundsKnown = false;
        nextID = 0;
    }

    /**
       Create a new GMLNode.
       @param x The X coordinate of the node in m.
       @param y The Y coordinate of the node in m.
       @return A new GMLNode with a unique ID.
    */
    public GMLNode createNode(double x, double y) {
        GMLNode n = new GMLNode(nextID++, x, y);
        addNode(n);
        return n;
    }

    /**
       Create a new GMLNode.
       @param coords The coordinates of the node.
       @return A new GMLNode with a unique ID.
    */
    public GMLNode createNode(GMLCoordinates coords) {
        GMLNode n = new GMLNode(nextID++, coords);
        addNode(n);
        return n;
    }

    /**
       Create a new GMLEdge between two nodes.
       @param first The 'start' node.
       @param second The 'end' node.
       @return A new GMLEdge with a unique ID.
    */
    public GMLEdge createEdge(GMLNode first, GMLNode second) {
        GMLEdge e = new GMLEdge(nextID++, first, second, false);
        addEdge(e);
        return e;
    }

    /**
       Create new GMLBuilding.
       @param apexes The apexes of the building.
       @return A new GMLBuilding with a unique ID.
    */
    public GMLBuilding createBuildingFromNodes(List<GMLNode> apexes) {
        return createBuilding(apexesToEdges(apexes));
    }

    /**
       Create new GMLBuilding.
       @param bEdges The edges of the building.
       @return A new GMLBuilding with a unique ID.
    */
    public GMLBuilding createBuilding(List<GMLDirectedEdge> bEdges) {
        GMLBuilding b = new GMLBuilding(nextID++, bEdges);
        addBuilding(b);
        return b;
    }

    /**
       Create new GMLRoad.
       @param apexes The apexes of the road.
       @return A new GMLRoad with a unique ID.
    */
    public GMLRoad createRoadFromNodes(List<GMLNode> apexes) {
        return createRoad(apexesToEdges(apexes));
    }

    /**
       Create new GMLRoad.
       @param rEdges The edges of the road.
       @return A new GMLRoad with a unique ID.
    */
    public GMLRoad createRoad(List<GMLDirectedEdge> rEdges) {
        GMLRoad r = new GMLRoad(nextID++, rEdges);
        addRoad(r);
        return r;
    }

    /**
       Create new GMLSpace.
       @param apexes The apexes of the space.
       @return A new GMLSpace with a unique ID.
    */
    public GMLSpace createSpaceFromNodes(List<GMLNode> apexes) {
        return createSpace(apexesToEdges(apexes));
    }

    /**
       Create new GMLSpace.
       @param sEdges The edges of the space.
       @return A new GMLSpace with a unique ID.
    */
    public GMLSpace createSpace(List<GMLDirectedEdge> sEdges) {
        GMLSpace s = new GMLSpace(nextID++, sEdges);
        addSpace(s);
        return s;
    }

    /**
       Add a node.
       @param n The node to add.
    */
    public void addNode(GMLNode n) {
        if (nodes.containsKey(n.getID())) {
            return;
        }
        nodes.put(n.getID(), n);
        allObjects.add(n);
        boundsKnown = false;
        nextID = Math.max(nextID, n.getID() + 1);
    }

    /**
       Add an edge. The edge's nodes will also be added if required.
       @param e The edge to add.
    */
    public void addEdge(GMLEdge e) {
        if (edges.containsKey(e.getID())) {
            return;
        }
        edges.put(e.getID(), e);
        allObjects.add(e);
        nextID = Math.max(nextID, e.getID() + 1);
        addNode(e.getStart());
        addNode(e.getEnd());
    }

    /**
       Add a building. The building's edges will be added if required.
       @param b The building to add.
    */
    public void addBuilding(GMLBuilding b) {
        if (buildings.containsKey(b.getID())) {
            return;
        }
        buildings.put(b.getID(), b);
        allShapes.add(b);
        allObjects.add(b);
        nextID = Math.max(nextID, b.getID() + 1);
        for (GMLDirectedEdge edge : b.getEdges()) {
            addEdge(edge.getEdge());
        }
    }

    /**
       Add a road. The road's edges will be added if required.
       @param r The road to add.
    */
    public void addRoad(GMLRoad r) {
        if (roads.containsKey(r.getID())) {
            return;
        }
        roads.put(r.getID(), r);
        allShapes.add(r);
        allObjects.add(r);
        nextID = Math.max(nextID, r.getID() + 1);
        for (GMLDirectedEdge edge : r.getEdges()) {
            addEdge(edge.getEdge());
        }
    }

    /**
       Add a space. The space's edges will be added if required.
       @param s The space to add.
    */
    public void addSpace(GMLSpace s) {
        if (spaces.containsKey(s.getID())) {
            return;
        }
        spaces.put(s.getID(), s);
        allShapes.add(s);
        allObjects.add(s);
        nextID = Math.max(nextID, s.getID() + 1);
        for (GMLDirectedEdge edge : s.getEdges()) {
            addEdge(edge.getEdge());
        }
    }

    /**
       Add an object.
       @param object The object to add.
    */
    public void add(GMLObject object) {
        if (object instanceof GMLNode) {
            addNode((GMLNode)object);
        }
        else if (object instanceof GMLEdge) {
            addEdge((GMLEdge)object);
        }
        else if (object instanceof GMLRoad) {
            addRoad((GMLRoad)object);
        }
        else if (object instanceof GMLBuilding) {
            addBuilding((GMLBuilding)object);
        }
        else if (object instanceof GMLSpace) {
            addSpace((GMLSpace)object);
        }
        else {
            throw new IllegalArgumentException("Don't know how to add " + object + " (class: " + object.getClass().getName() + ")");
        }
    }

    /**
       Add a set of objects.
       @param objects The objects to add.
    */
    public void add(Collection<? extends GMLObject> objects) {
        for (GMLObject next : objects) {
            add(next);
        }
    }

    /**
       Add a set of objects.
       @param objects The objects to add.
    */
    public void add(GMLObject... objects) {
        for (GMLObject next : objects) {
            add(next);
        }
    }

    /**
       Remove a node, any edges attached to the node and any shapes attached to those edges.
       @param n The node to remove.
       @return All removed objects, not including n.
    */
    public Collection<GMLObject> removeNode(GMLNode n) {
        Collection<GMLObject> result = new HashSet<GMLObject>();
        if (nodes.containsKey(n.getID())) {
            nodes.remove(n.getID());
            allObjects.remove(n);
            for (GMLEdge next : getAttachedEdges(n)) {
                result.add(next);
                result.addAll(removeEdge(next));
            }
            boundsKnown = false;
        }
        return result;
    }

    /**
       Remove an edge and any attached shapes.
       @param e The edge to remove.
       @return All removed objects, not including e.
    */
    public Collection<GMLObject> removeEdge(GMLEdge e) {
        Collection<GMLObject> result = new HashSet<GMLObject>();
        if (edges.containsKey(e.getID())) {
            edges.remove(e.getID());
            allObjects.remove(e);
            for (GMLShape next : getAttachedShapes(e)) {
                result.add(next);
                remove(next);
            }
        }
        return result;
    }

    /**
       Remove a building.
       @param b The building to remove.
    */
    public void removeBuilding(GMLBuilding b) {
        buildings.remove(b.getID());
        allShapes.remove(b);
        allObjects.remove(b);
    }

    /**
       Remove a road.
       @param r The road to remove.
    */
    public void removeRoad(GMLRoad r) {
        roads.remove(r.getID());
        allShapes.remove(r);
        allObjects.remove(r);
    }

    /**
       Remove a space.
       @param s The space to remove.
    */
    public void removeSpace(GMLSpace s) {
        spaces.remove(s.getID());
        allShapes.remove(s);
        allObjects.remove(s);
    }

    /**
       Remove an object.
       @param object The object to remove.
    */
    public void remove(GMLObject object) {
        if (object instanceof GMLNode) {
            removeNode((GMLNode)object);
        }
        else if (object instanceof GMLEdge) {
            removeEdge((GMLEdge)object);
        }
        else if (object instanceof GMLRoad) {
            removeRoad((GMLRoad)object);
        }
        else if (object instanceof GMLBuilding) {
            removeBuilding((GMLBuilding)object);
        }
        else if (object instanceof GMLSpace) {
            removeSpace((GMLSpace)object);
        }
        else {
            throw new IllegalArgumentException("Don't know how to remove " + object + " (class: " + object.getClass().getName() + ")");
        }
    }

    /**
       Remove a set of objects.
       @param objects The objects to remove.
    */
    public void remove(Collection<? extends GMLObject> objects) {
        for (GMLObject next : objects) {
            remove(next);
        }
    }

    /**
       Remove a set of objects.
       @param objects The objects to remove.
    */
    public void remove(GMLObject... objects) {
        for (GMLObject next : objects) {
            remove(next);
        }
    }

    /**
       Remove all nodes, edges and shapes.
    */
    public void removeAllNodes() {
        nodes.clear();
        edges.clear();
        roads.clear();
        buildings.clear();
        spaces.clear();
        allShapes.clear();
        allObjects.clear();
        boundsKnown = false;
    }

    /**
       Remove all edges and shapes.
    */
    public void removeAllEdges() {
        edges.clear();
        roads.clear();
        buildings.clear();
        spaces.clear();
        allShapes.clear();
        allObjects.retainAll(nodes.values());
    }

    /**
       Remove all buildings.
    */
    public void removeAllBuildings() {
        allShapes.removeAll(buildings.values());
        allObjects.removeAll(buildings.values());
        buildings.clear();
    }

    /**
       Remove all roads.
    */
    public void removeAllRoads() {
        allShapes.removeAll(roads.values());
        allObjects.removeAll(roads.values());
        roads.clear();
    }

    /**
       Remove all spaces.
    */
    public void removeAllSpaces() {
        allShapes.removeAll(spaces.values());
        allObjects.removeAll(spaces.values());
        spaces.clear();
    }

    /**
       Get a node by ID.
       @param id The ID to look up.
       @return The node with that ID or null if the ID is not found.
    */
    public GMLNode getNode(int id) {
        return nodes.get(id);
    }

    /**
       Get an edge by ID.
       @param id The ID to look up.
       @return The edge with that ID or null if the ID is not found.
    */
    public GMLEdge getEdge(int id) {
        return edges.get(id);
    }

    /**
       Get a building by ID.
       @param id The ID to look up.
       @return The building with that ID or null if the ID is not found.
    */
    public GMLBuilding getBuilding(int id) {
        return buildings.get(id);
    }

    /**
       Get a road by ID.
       @param id The ID to look up.
       @return The road with that ID or null if the ID is not found.
    */
    public GMLRoad getRoad(int id) {
        return roads.get(id);
    }

    /**
       Get a space by ID.
       @param id The ID to look up.
       @return The space with that ID or null if the ID is not found.
    */
    public GMLSpace getSpace(int id) {
        return spaces.get(id);
    }

    /**
       Get all nodes in the map.
       @return All nodes.
    */
    public Set<GMLNode> getNodes() {
        return new HashSet<GMLNode>(nodes.values());
    }

    /**
       Get all edges in the map.
       @return All edges.
    */
    public Set<GMLEdge> getEdges() {
        return new HashSet<GMLEdge>(edges.values());
    }

    /**
       Get all buildings in the map.
       @return All buildings.
    */
    public Set<GMLBuilding> getBuildings() {
        return new HashSet<GMLBuilding>(buildings.values());
    }

    /**
       Get all roads in the map.
       @return All roads.
    */
    public Set<GMLRoad> getRoads() {
        return new HashSet<GMLRoad>(roads.values());
    }

    /**
       Get all spaces in the map.
       @return All spaces.
    */
    public Set<GMLSpace> getSpaces() {
        return new HashSet<GMLSpace>(spaces.values());
    }

    /**
       Get all shapes in the map.
       @return All shapes.
    */
    public Set<GMLShape> getAllShapes() {
        return Collections.unmodifiableSet(allShapes);
    }

    /**
       Get all objects in the map.
       @return All objects.
    */
    public Set<GMLObject> getAllObjects() {
        return Collections.unmodifiableSet(allObjects);
    }

    /**
       Get the minimum x coordinate.
       @return The minimum x coordinate.
     */
    public double getMinX() {
        calculateBounds();
        return minX;
    }

    /**
       Get the maximum x coordinate.
       @return The maximum x coordinate.
     */
    public double getMaxX() {
        calculateBounds();
        return maxX;
    }

    /**
       Get the minimum y coordinate.
       @return The minimum y coordinate.
     */
    public double getMinY() {
        calculateBounds();
        return minY;
    }

    /**
       Get the maximum y coordinate.
       @return The maximum y coordinate.
     */
    public double getMaxY() {
        calculateBounds();
        return maxY;
    }

    /**
       Rescale the map coordinates.
       @param conversion The coordinate conversion to apply.
    */
    public void convertCoordinates(CoordinateConversion conversion) {
        for (GMLNode next : nodes.values()) {
            next.convert(conversion);
        }
    }

    /**
       Create or retrieve an existing edge between two nodes.
       @param first The 'start' node.
       @param second The 'end' node.
       @return A new GMLEdge with a unique ID or an existing edge. The returned edge may be reversed with respect to first and second.
    */
    public GMLEdge ensureEdge(GMLNode first, GMLNode second) {
        for (GMLEdge next : edges.values()) {
            if ((next.getStart().equals(first) && next.getEnd().equals(second))
                || (next.getStart().equals(second) && next.getEnd().equals(first))
                ) {
                return next;
            }
        }
        return createEdge(first, second);
    }

    /**
       Turn a list of apexes into a list of directed edges.
       @param apexes The apexes to convert.
       @return A list of directed edges.
    */
    public List<GMLDirectedEdge> apexesToEdges(List<GMLNode> apexes) {
        List<GMLDirectedEdge> edgeList = new ArrayList<GMLDirectedEdge>(apexes.size());
        Iterator<GMLNode> it = apexes.iterator();
        GMLNode first = it.next();
        GMLNode previous = first;
        while (it.hasNext()) {
            GMLNode next = it.next();
            GMLEdge edge = ensureEdge(previous, next);
            edgeList.add(new GMLDirectedEdge(edge, previous));
            previous = next;
        }
        GMLEdge edge = ensureEdge(previous, first);
        edgeList.add(new GMLDirectedEdge(edge, previous));
        return edgeList;
    }

    /**
       Get all GMLNodes inside a region.
       @param xMin The lower X bound of the region.
       @param yMin The lower Y bound of the region.
       @param xMax The upper X bound of the region.
       @param yMax The upper Y bound of the region.
       @return All GMLNodes inside the region.
    */
    public Collection<GMLNode> getNodesInRegion(double xMin, double yMin, double xMax, double yMax) {
        Collection<GMLNode> result = new ArrayList<GMLNode>();
        for (GMLNode next : nodes.values()) {
            double x = next.getX();
            double y = next.getY();
            if (x >= xMin && x <= xMax && y >= yMin && y <= yMax) {
                result.add(next);
            }
        }
        return result;
    }

    /**
       Find the GMLNode nearest a point.
       @param x The X coordinate.
       @param y The Y coordinate.
       @return The nearest GMLNode.
    */
    public GMLNode findNearestNode(double x, double y) {
        GMLNode best = null;
        double bestDistance = Double.NaN;
        for (GMLNode next : nodes.values()) {
            double dx = x - next.getX();
            double dy = y - next.getY();
            double d = (dx * dx) + (dy * dy);
            if (best == null || d < bestDistance) {
                best = next;
                bestDistance = d;
            }
        }
        return best;
    }

    /**
       Find the GMLEdge nearest a point.
       @param x The X coordinate.
       @param y The Y coordinate.
       @return The nearest GMLEdge.
    */
    public GMLEdge findNearestEdge(double x, double y) {
        GMLEdge best = null;
        double bestDistance = Double.NaN;
        Point2D test = new Point2D(x, y);
        for (GMLEdge next : edges.values()) {
            Line2D line = GMLTools.toLine(next);
            Point2D closest = GeometryTools2D.getClosestPointOnSegment(line, test);
            double d = GeometryTools2D.getDistance(test, closest);
            if (best == null || d < bestDistance) {
                best = next;
                bestDistance = d;
            }
        }
        return best;
    }

    /**
       Find the GMLShape under a point.
       @param x The X coordinate.
       @param y The Y coordinate.
       @return The shape under the point or null if no shapes are found.
    */
    public GMLShape findShapeUnder(double x, double y) {
        for (GMLShape next : allShapes) {
            if (GMLTools.coordsToShape(next.getUnderlyingCoordinates()).contains(x, y)) {
                return next;
            }
        }
        return null;
    }

    /**
       Get all GMLEdges that are attached to a GMLNode.
       @param node The GMLNode to look up.
       @return All attached GMLEdges.
    */
    public Collection<GMLEdge> getAttachedEdges(GMLNode node) {
        Collection<GMLEdge> result = new HashSet<GMLEdge>();
        for (GMLEdge next : edges.values()) {
            if (next.getStart().equals(node) || next.getEnd().equals(node)) {
                result.add(next);
            }
        }
        return result;
    }

    /**
       Get all GMLShapes that are attached to a GMLEdge.
       @param edge The GMLEdge to look up.
       @return All attached GMLShapes.
    */
    public Collection<GMLShape> getAttachedShapes(GMLEdge edge) {
        Collection<GMLShape> result = new HashSet<GMLShape>();
        for (GMLShape next : allShapes) {
            for (GMLDirectedEdge nextEdge : next.getEdges()) {
                if (nextEdge.getEdge().equals(edge)) {
                    result.add(next);
                    break;
                }
            }
        }
        return result;
    }

    /**
       Merge a pair of edges and form a new edge. The node and existing edges are NOT removed from the map.
       @param edge1 The first edge.
       @param edge2 The second edge.
       @return The new edge.
    */
    public GMLEdge mergeEdges(GMLEdge edge1, GMLEdge edge2) {
        GMLNode commonNode = edge1.getStart();
        if (!commonNode.equals(edge2.getStart()) && !commonNode.equals(edge2.getEnd())) {
            commonNode = edge1.getEnd();
        }
        if (!commonNode.equals(edge2.getStart()) && !commonNode.equals(edge2.getEnd())) {
            throw new IllegalArgumentException("Edges " + edge1 + " and " + edge2 + " do not have a common node");
        }
        GMLNode start = commonNode.equals(edge1.getStart()) ? edge1.getEnd() : edge1.getStart();
        GMLNode end = commonNode.equals(edge2.getStart()) ? edge2.getEnd() : edge2.getStart();
        return ensureEdge(start, end);
    }

    private void calculateBounds() {
        if (boundsKnown) {
            return;
        }
        minX = Double.POSITIVE_INFINITY;
        minY = Double.POSITIVE_INFINITY;
        maxX = Double.NEGATIVE_INFINITY;
        maxY = Double.NEGATIVE_INFINITY;
        for (GMLNode n : nodes.values()) {
            GMLCoordinates c = n.getCoordinates();
            minX = Math.min(minX, c.getX());
            maxX = Math.max(maxX, c.getX());
            minY = Math.min(minY, c.getY());
            maxY = Math.max(maxY, c.getY());
        }
        boundsKnown = true;
    }
}
