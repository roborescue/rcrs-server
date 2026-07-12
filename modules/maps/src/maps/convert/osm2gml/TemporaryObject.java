package maps.convert.osm2gml;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Path2D;
import java.awt.geom.Area;
import java.awt.Shape;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;
import maps.convert.osm2gml.debug.Polygonal;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;

import maps.gml.GMLCoordinates;
import maps.gml.GMLTools;

/**
 * Abstract base class for temporary data structures during conversion.
 */
public abstract class TemporaryObject implements SpatialIndexable, Polygonal {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    @Getter
    private final long id;
    private final List<DirectedEdge> edges;
    private final Map<DirectedEdge, TemporaryObject> neighbours;

    // The following properties are cached for performance.
    private Path2D path;
    private Rectangle2D bounds;
    private Point2D centroid;

    /**
     * Construct a new TemporaryObject.
     * @param edges The edges of the object in counter-clockwise order.
     */
    protected TemporaryObject(final List<DirectedEdge> edges) {
        this.id = ID_GENERATOR.getAndIncrement();
        this.edges = new ArrayList<>(edges);
        this.neighbours = new HashMap<>();
    }

    /**
       Get the edges of this object.
       @return The edges.
    */
    public List<DirectedEdge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Get the nodes of this object.
     * @return The nodes.
     */
    public List<Node> getNodes() {
        final List<Node> nodes = new ArrayList<>();
        if (edges.isEmpty()) return Collections.unmodifiableList(nodes);

        // Collect the start node of each edge; this covers all vertices without duplication.
        final Node first = edges.getFirst().getStartNode();
        for (final DirectedEdge edge : edges) {
            final Node start = edge.getStartNode();
            if (!start.equals(first) || nodes.isEmpty()) nodes.add(start);
        }

        return Collections.unmodifiableList(nodes);
    }

    /**
     * Get the list of vertices (coordinates) of this object as a closed polygon.
     * The first vertex is automatically added at the end to ensure the polygon is closed.
     * @return An unmodifiable list of Point2D representing the vertices of the object.
     */
    @Override
    public List<Point2D> getVertices() {
        final List<Point2D> vertices = new ArrayList<>();
        if (edges.isEmpty()) return Collections.unmodifiableList(vertices);

        // Add the start node of each edge: the last edge's and equal the first's start,
        // so adding it explicitly closes the polygon without duplication.
        for (final DirectedEdge edge : edges) {
            vertices.add(edge.getStartCoordinates());
        }
        vertices.add(edges.getFirst().getStartCoordinates());

        return Collections.unmodifiableList(vertices);
    }

    /**
       Get the neighbour through a particular edge.
       @param edge The edge to look up.
       @return The neighbour through that edge or null.
    */
    public TemporaryObject getNeighbour(DirectedEdge edge) {
        return neighbours.get(edge);
    }

    /**
       Set the neighbour through a particular edge.
       @param edge The edge to set the neighbour of.
       @param neighbour The new neighbour for that edge.
    */
    public void setNeighbour(DirectedEdge edge, TemporaryObject neighbour) {
        neighbours.put(edge, neighbour);
    }

    /**
       Set the neighbour through a particular edge.
       @param edge The edge to set the neighbour of.
       @param neighbour The new neighbour for that edge.
    */
    public void setNeighbour(Edge edge, TemporaryObject neighbour) {
        neighbours.put(findDirectedEdge(edge), neighbour);
    }

    /**
       Turn the edges into a list of coordinates.
       @return A list of GMLCoordinates.
    */
    public List<GMLCoordinates> makeGMLCoordinates() {
        List<GMLCoordinates> result = new ArrayList<>();
        for (DirectedEdge next : edges) {
            Point2D p = next.getStartCoordinates();
            result.add(new GMLCoordinates(p.getX(), p.getY()));
        }
        return result;
    }

    /**
       Get the bounds of this object.
       @return The bounds.
    */
    @Override
    public Rectangle2D getBounds() {
        if (bounds == null) {
            bounds = GMLTools.getBounds(makeGMLCoordinates());
        }
        return bounds;
    }

    /**
       Get the Shape of this object.
       @return The shape.
    */
    public Shape getShape() {
        if (path == null) {
            path = new Path2D.Double();
            Iterator<DirectedEdge> it = edges.iterator();
            DirectedEdge d = it.next();
            path.moveTo(d.getStartCoordinates().getX(), d.getStartCoordinates().getY());
            path.lineTo(d.getEndCoordinates().getX(), d.getEndCoordinates().getY());
            while (it.hasNext()) {
                d = it.next();
                path.lineTo(d.getEndCoordinates().getX(), d.getEndCoordinates().getY());
            }
        }
        return path;
    }

    /**
       Check if this object is a duplicate of another. Objects are duplicates if they contain the same list of directed edges, possibly offset.
       @param other The other object to check against.
       @return True if this object is a duplicate of other, false otherwise.
    */
    public boolean isDuplicate(TemporaryObject other) {
        List<DirectedEdge> myEdges = getEdges();
        List<DirectedEdge> otherEdges = other.getEdges();
        if (myEdges.size() != otherEdges.size()) {
            return false;
        }
        Iterator<DirectedEdge> it = myEdges.iterator();
        DirectedEdge start = it.next();
        // See if we can find an equivalent edge in other
        Iterator<DirectedEdge> ix = otherEdges.iterator();
        DirectedEdge otherStart = null;
        while (ix.hasNext()) {
            DirectedEdge test = ix.next();
            if (test.equals(start)) {
                // Found!
                otherStart = test;
                break;
            }
        }
        if (otherStart == null) {
            // Edge not found in other so can't be a duplicate
            return false;
        }
        // Check that edges are equivalent
        // Walk through the edge lists starting at the beginning for me and at the equivalent edge in others. When we reach the end of other go back to the start.
        while (ix.hasNext()) {
            DirectedEdge a = it.next();
            DirectedEdge b = ix.next();
            if (!a.equals(b)) {
                return false;
            }
        }
        ix = otherEdges.iterator();
        while (it.hasNext()) {
            DirectedEdge a = it.next();
            DirectedEdge b = ix.next();
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
    }

    /**
       Check if this object is an entirely inside another.
       @param other The other object to check against.
       @return True if this object is entirely inside the other, false otherwise.
    */
    public boolean isEntirelyInside(TemporaryObject other) {
        if (!this.getBounds().intersects(other.getBounds())) {
            return false;
        }
        Area a = new Area(getShape());
        Area b = new Area(other.getShape());
        Area intersection = new Area(a);
        intersection.intersect(b);
        return a.equals(intersection);
    }

    /**
       Replace an edge with a set of replacement edges.
       @param edge The edge to replace.
       @param replacements The set of replacement edges. These can be in any order.
    */
    protected void replaceEdge(Edge edge, Collection<Edge> replacements) {
        if (replacements.isEmpty()) {
            // Just remove the edge
            edges.removeIf(next -> next.getEdge().equals(edge));
        }
        else {
            for (ListIterator<DirectedEdge> it = edges.listIterator(); it.hasNext();) {
                DirectedEdge next = it.next();
                if (next.getEdge().equals(edge)) {
                    it.remove();
                    Set<Edge> replacementsSet = new HashSet<>(replacements);
                    // Create directed edges for the replacements
                    Node start = next.getStartNode();
                    Node end = next.getEndNode();
                    while (!start.equals(end)) {
                        DirectedEdge newEdge = findNewEdge(start, replacementsSet);
                        replacementsSet.remove(Objects.requireNonNull(newEdge).getEdge());
                        it.add(newEdge);
                        start = newEdge.getEndNode();
                    }
                    break;
                }
            }
        }
        bounds = null;
        path = null;
        centroid = null;
    }

    private DirectedEdge findNewEdge(Node from, Set<Edge> candidates) {
        for (Edge next : candidates) {
            if (next.getStart().equals(from)) {
                return new DirectedEdge(next, true);
            }
            if (next.getEnd().equals(from)) {
                return new DirectedEdge(next, false);
            }
        }
        return null;
    }

    private DirectedEdge findDirectedEdge(Edge e) {
        for (DirectedEdge next : edges) {
            if (next.getEdge().equals(e)) {
                return next;
            }
        }
        throw new IllegalArgumentException("Edge " + e + " not found");
    }

    /**
     * Compute the centroid of this object.
     * The centroid is calculated from the vertices of the object treated as a closed polygon.
     * @return The centroid as a Point2D.
     */
    public Point2D getCentroid() {
        if (centroid == null) {
            centroid = GeometryTools2D.computeCentroid(getVertices());
        }
        return centroid;
    }

    /**
     * Get the edges of this object as Line2D.
     * @return A list of Line2D representing the edges of this object.
     */
    public List<Line2D> getLines() {
        List<Line2D> lines = new ArrayList<>();
        if (edges.isEmpty()) return Collections.unmodifiableList(lines);
        for (DirectedEdge edge : edges) {
            lines.add(edge.getLine());
        }
        return Collections.unmodifiableList(lines);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + id;
    }
}
