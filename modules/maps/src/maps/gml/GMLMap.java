package maps.gml;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import maps.CoordinateConversion;

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
        nodes.put(n.getID(), n);
        boundsKnown = false;
        nextID = Math.max(nextID, n.getID() + 1);
    }

    /**
       Remove a node.
       @param n The node to remove.
    */
    public void removeNode(GMLNode n) {
        nodes.remove(n.getID());
        boundsKnown = false;
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
       Get all nodes in the map.
       @return All nodes.
    */
    public Set<GMLNode> getNodes() {
        return new HashSet<GMLNode>(nodes.values());
    }

    /**
       Remove all nodes.
    */
    public void removeAllNodes() {
        nodes.clear();
        boundsKnown = false;
    }

    /**
       Add an edge.
       @param e The edge to add.
    */
    public void addEdge(GMLEdge e) {
        edges.put(e.getID(), e);
        nextID = Math.max(nextID, e.getID() + 1);
    }

    /**
       Remove an edge.
       @param e The edge to remove.
    */
    public void removeEdge(GMLEdge e) {
        edges.remove(e.getID());
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
       Get all edges in the map.
       @return All edges.
    */
    public Set<GMLEdge> getEdges() {
        return new HashSet<GMLEdge>(edges.values());
    }

    /**
       Remove all edges.
    */
    public void removeAllEdges() {
        edges.clear();
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
       Add a building.
       @param b The building to add.
    */
    public void addBuilding(GMLBuilding b) {
        buildings.put(b.getID(), b);
        allShapes.add(b);
        nextID = Math.max(nextID, b.getID() + 1);
    }

    /**
       Remove a building.
       @param b The building to remove.
    */
    public void removeBuilding(GMLBuilding b) {
        buildings.remove(b.getID());
        allShapes.remove(b);
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
       Get all buildings in the map.
       @return All buildings.
    */
    public Set<GMLBuilding> getBuildings() {
        return new HashSet<GMLBuilding>(buildings.values());
    }

    /**
       Remove all buildings.
    */
    public void removeAllBuildings() {
        allShapes.removeAll(buildings.values());
        buildings.clear();
    }

    /**
       Add a road.
       @param r The road to add.
    */
    public void addRoad(GMLRoad r) {
        roads.put(r.getID(), r);
        allShapes.add(r);
        nextID = Math.max(nextID, r.getID() + 1);
    }

    /**
       Remove a road.
       @param r The road to remove.
    */
    public void removeRoad(GMLRoad r) {
        roads.remove(r.getID());
        allShapes.remove(r);
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
       Get all roads in the map.
       @return All roads.
    */
    public Set<GMLRoad> getRoads() {
        return new HashSet<GMLRoad>(roads.values());
    }

    /**
       Remove all roads.
    */
    public void removeAllRoads() {
        allShapes.removeAll(roads.values());
        roads.clear();
    }

    /**
       Add a space.
       @param s The space to add.
    */
    public void addSpace(GMLSpace s) {
        spaces.put(s.getID(), s);
        allShapes.add(s);
        nextID = Math.max(nextID, s.getID() + 1);
    }

    /**
       Remove a space.
       @param s The space to remove.
    */
    public void removeSpace(GMLSpace s) {
        spaces.remove(s.getID());
        allShapes.remove(s);
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
       Get all spaces in the map.
       @return All spaces.
    */
    public Set<GMLSpace> getSpaces() {
        return new HashSet<GMLSpace>(spaces.values());
    }

    /**
       Remove all spaces.
    */
    public void removeAllSpaces() {
        allShapes.removeAll(spaces.values());
        spaces.clear();
    }

    /**
       Get all shapes in the map.
       @return All shapes.
    */
    public Set<GMLShape> getAllShapes() {
        return Collections.unmodifiableSet(allShapes);
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
