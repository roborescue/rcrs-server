package maps.gml;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import maps.CoordinateConversion;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;

import rescuecore2.misc.collections.LazyMap;

/**
   A GML map. All coordinates are specified in m.
*/
public class GMLMap implements maps.Map {
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

    private Map<GMLNode, Collection<GMLEdge>> attachedEdges;
    private Map<GMLEdge, Collection<GMLShape>> attachedShapes;

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
        attachedEdges = new LazyMap<GMLNode, Collection<GMLEdge>>() {
            @Override
            public Collection<GMLEdge> createValue() {
                return new HashSet<GMLEdge>();
            }
        };
        attachedShapes = new LazyMap<GMLEdge, Collection<GMLShape>>() {
            @Override
            public Collection<GMLShape> createValue() {
                return new HashSet<GMLShape>();
            }
        };
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
       Create a set of new GMLNodes.
       @param coords The coordinates of the new nodes.
       @return A set of new GMLNodes with unique IDs.
    */
    public List<GMLNode> createNodes(List<GMLCoordinates> coords) {
        List<GMLNode> result = new ArrayList<GMLNode>(coords.size());
        for (GMLCoordinates c : coords) {
            GMLNode n = new GMLNode(nextID++, c);
            addNode(n);
            result.add(n);
        }
        return result;
    }

    /**
       Create a set of new GMLNodes.
       @param coords The coordinates of the new nodes.
       @return A set of new GMLNodes with unique IDs.
    */
    public List<GMLNode> createNodesFromPoints(List<Point2D> coords) {
        List<GMLNode> result = new ArrayList<GMLNode>(coords.size());
        for (Point2D p : coords) {
            GMLNode n = new GMLNode(nextID++, p.getX(), p.getY());
            addNode(n);
            result.add(n);
        }
        return result;
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
        b.setFloors(1);
        b.setCode(0);
        b.setImportance(1);
        b.setCapacity(0);//todo
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
        addObject(n);
        nodes.put(n.getID(), n);
        boundsKnown = false;
    }

    /**
       Add an edge. The edge's nodes will also be added if required.
       @param e The edge to add.
    */
    public void addEdge(GMLEdge e) {
        if (edges.containsKey(e.getID())) {
            return;
        }
        addObject(e);
        edges.put(e.getID(), e);
        addNode(e.getStart());
        addNode(e.getEnd());
        attachedEdges.get(e.getStart()).add(e);
        attachedEdges.get(e.getEnd()).add(e);
    }

    /**
       Add a building. The building's edges will be added if required.
       @param b The building to add.
    */
    public void addBuilding(GMLBuilding b) {
        if (buildings.containsKey(b.getID())) {
            return;
        }
        addShape(b);
        buildings.put(b.getID(), b);
    }

    /**
       Add a road. The road's edges will be added if required.
       @param r The road to add.
    */
    public void addRoad(GMLRoad r) {
        if (roads.containsKey(r.getID())) {
            return;
        }
        addShape(r);
        roads.put(r.getID(), r);
    }

    /**
       Add a space. The space's edges will be added if required.
       @param s The space to add.
    */
    public void addSpace(GMLSpace s) {
        if (spaces.containsKey(s.getID())) {
            return;
        }
        addShape(s);
        spaces.put(s.getID(), s);
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
            removeObject(n);
            nodes.remove(n.getID());
            Collection<GMLEdge> attached = new HashSet<GMLEdge>(getAttachedEdges(n));
            for (GMLEdge next : attached) {
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
            removeObject(e);
            edges.remove(e.getID());
            Collection<GMLShape> attached = new HashSet<GMLShape>(getAttachedShapes(e));
            for (GMLShape next : attached) {
                result.add(next);
                remove(next);
            }
            attachedEdges.get(e.getStart()).remove(e);
            attachedEdges.get(e.getEnd()).remove(e);
        }
        return result;
    }

    /**
       Remove a building.
       @param b The building to remove.
    */
    public void removeBuilding(GMLBuilding b) {
        if (buildings.containsKey(b.getID())) {
            removeShape(b);
            buildings.remove(b.getID());
        }
    }

    /**
       Remove a road.
       @param r The road to remove.
    */
    public void removeRoad(GMLRoad r) {
        if (roads.containsKey(r.getID())) {
            removeShape(r);
            roads.remove(r.getID());
        }
    }

    /**
       Remove a space.
       @param s The space to remove.
    */
    public void removeSpace(GMLSpace s) {
        if (spaces.containsKey(s.getID())) {
            removeShape(s);
            spaces.remove(s.getID());
        }
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
        attachedEdges.clear();
        attachedShapes.clear();
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
        attachedEdges.clear();
        attachedShapes.clear();
    }

    /**
       Remove all buildings.
    */
    public void removeAllBuildings() {
        for (Map.Entry<GMLEdge, Collection<GMLShape>> entry : attachedShapes.entrySet()) {
            entry.getValue().removeAll(buildings.values());
        }
        allShapes.removeAll(buildings.values());
        allObjects.removeAll(buildings.values());
        buildings.clear();
    }

    /**
       Remove all roads.
    */
    public void removeAllRoads() {
        for (Map.Entry<GMLEdge, Collection<GMLShape>> entry : attachedShapes.entrySet()) {
            entry.getValue().removeAll(buildings.values());
        }
        allShapes.removeAll(roads.values());
        allObjects.removeAll(roads.values());
        roads.clear();
    }

    /**
       Remove all spaces.
    */
    public void removeAllSpaces() {
        for (Map.Entry<GMLEdge, Collection<GMLShape>> entry : attachedShapes.entrySet()) {
            entry.getValue().removeAll(buildings.values());
        }
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
       Get a shape by ID.
       @param id The ID to look up.
       @return The shape with that ID or null if the ID is not found.
    */
    public GMLShape getShape(int id) {
        GMLBuilding b = getBuilding(id);
        if (b != null) {
            return b;
        }
        GMLRoad r = getRoad(id);
        if (r != null) {
            return r;
        }
        GMLSpace s = getSpace(id);
        if (s != null) {
            return s;
        }
        return null;
    }

    /**
       Get an object by ID.
       @param id The ID to look up.
       @return The object with that ID or null if the ID is not found.
    */
    public GMLObject getObject(int id) {
        GMLNode n = getNode(id);
        if (n != null) {
            return n;
        }
        GMLEdge e = getEdge(id);
        if (e != null) {
            return e;
        }
        GMLBuilding b = getBuilding(id);
        if (b != null) {
            return b;
        }
        GMLRoad r = getRoad(id);
        if (r != null) {
            return r;
        }
        GMLSpace s = getSpace(id);
        if (s != null) {
            return s;
        }
        return null;
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
       Find out if this map has a real size or not. Maps with zero or one nodes do not have a real size.
       @return True if this map has two or more nodes.
    */
    public boolean hasSize() {
        return nodes.size() > 1;
    }

    /**
       Rescale the map coordinates.
       @param conversion The coordinate conversion to apply.
    */
    public void convertCoordinates(CoordinateConversion conversion) {
        for (GMLNode next : nodes.values()) {
            next.convert(conversion);
        }
        boundsKnown = false;
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
    public List<GMLDirectedEdge> apexesToEdges(GMLNode... apexes) {
        return apexesToEdges(Arrays.asList(apexes));
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
        return findNearestEdge(x, y, edges.values());
    }

    /**
       Find the GMLEdge nearest a point from a set of possible edges.
       @param x The X coordinate.
       @param y The Y coordinate.
       @param possible The set of possible edges.
       @return The nearest GMLEdge.
    */
    public GMLEdge findNearestEdge(double x, double y, Collection<? extends GMLEdge> possible) {
        GMLEdge best = null;
        double bestDistance = Double.NaN;
        Point2D test = new Point2D(x, y);
        for (GMLEdge next : possible) {
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
        return Collections.unmodifiableCollection(attachedEdges.get(node));
    }

    /**
       Get all GMLShapes that are attached to a GMLEdge.
       @param edge The GMLEdge to look up.
       @return All attached GMLShapes.
    */
    public Collection<GMLShape> getAttachedShapes(GMLEdge edge) {
        return Collections.unmodifiableCollection(attachedShapes.get(edge));
    }

    /**
       Merge a pair of edges and form a new edge. The node and existing edges are not removed from the map.
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

    /**
       Replace all references to a node with another node. This does not delete the old node from the map.
       @param oldNode The node to replace.
       @param newNode The new node.
    */
    public void replaceNode(GMLNode oldNode, GMLNode newNode) {
        List<GMLEdge> attached = new ArrayList<GMLEdge>(getAttachedEdges(oldNode));
        for (GMLEdge next : attached) {
            if (next.getStart().equals(oldNode)) {
                next.setStart(newNode);
                attachedEdges.get(oldNode).remove(next);
                attachedEdges.get(newNode).add(next);
            }
            if (next.getEnd().equals(oldNode)) {
                next.setEnd(newNode);
                attachedEdges.get(oldNode).remove(next);
                attachedEdges.get(newNode).add(next);
            }
        }
    }

    /**
       Replace all references to an edge with another edge. This does not delete the old edge from the map. The two edges must have the same pair of start and end nodes but may be in different directions.
       @param oldEdge The edge to replace.
       @param newEdge The new edge.
    */
    public void replaceEdge(GMLEdge oldEdge, GMLEdge newEdge) {
        if ((oldEdge.getStart() != newEdge.getStart() && oldEdge.getStart() != newEdge.getEnd())
            || (oldEdge.getEnd() != newEdge.getStart() && oldEdge.getEnd() != newEdge.getEnd())) {
            throw new IllegalArgumentException("oldEdge and newEdge do not share start and end nodes");
        }
        Collection<GMLShape> attached = new HashSet<GMLShape>(getAttachedShapes(oldEdge));
        for (GMLShape next : attached) {
            for (GMLDirectedEdge dEdge : next.getEdges()) {
                if (dEdge.getEdge() == oldEdge) {
                    boolean forward;
                    if (oldEdge.getStart() == newEdge.getStart()) {
                        forward = dEdge.isForward();
                    }
                    else {
                        forward = !dEdge.isForward();
                    }
                    GMLDirectedEdge replacement = new GMLDirectedEdge(newEdge, forward);
                    next.replaceEdge(dEdge, replacement);
                    attachedShapes.get(oldEdge).remove(next);
                    attachedShapes.get(newEdge).add(next);
                }
            }
        }
    }

    /**
       Insert a node into an edge. This method updates attached edges but does not delete the old edge.
       @param edge The edge to split.
       @param node The node to insert.
       @return The two new edges.
    */
    public Collection<GMLEdge> splitEdge(GMLEdge edge, GMLNode node) {
        Collection<GMLEdge> result = new ArrayList<GMLEdge>(2);
        GMLEdge first = ensureEdge(edge.getStart(), node);
        GMLEdge second = ensureEdge(node, edge.getEnd());
        result.add(first);
        result.add(second);
        // Update any attached edges
        Collection<GMLShape> attached = new HashSet<GMLShape>(getAttachedShapes(edge));
        for (GMLShape shape : attached) {
            for (GMLDirectedEdge dEdge : shape.getEdges()) {
                if (dEdge.getEdge() == edge) {
                    // Create two new directed edges
                    GMLDirectedEdge d1;
                    GMLDirectedEdge d2;
                    if (dEdge.isForward()) {
                        d1 = new GMLDirectedEdge(first, true);
                        d2 = new GMLDirectedEdge(second, true);
                    }
                    else {
                        d1 = new GMLDirectedEdge(second, false);
                        d2 = new GMLDirectedEdge(first, false);
                    }
                    shape.replaceEdge(dEdge, d1, d2);
                    attachedShapes.get(edge).remove(shape);
                    attachedShapes.get(first).add(shape);
                    attachedShapes.get(second).add(shape);
                }
            }
        }
        return result;
    }

    private void addShape(GMLShape shape) {
        addObject(shape);
        allShapes.add(shape);
        for (GMLDirectedEdge edge : shape.getEdges()) {
            addEdge(edge.getEdge());
            attachedShapes.get(edge.getEdge()).add(shape);
        }
    }

    private void addObject(GMLObject object) {
        allObjects.add(object);
        nextID = Math.max(nextID, object.getID() + 1);
    }

    private void removeShape(GMLShape shape) {
        removeObject(shape);
        allShapes.remove(shape);
        for (GMLDirectedEdge edge : shape.getEdges()) {
            attachedShapes.get(edge.getEdge()).remove(shape);
        }
    }

    private void removeObject(GMLObject object) {
        allObjects.remove(object);
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
