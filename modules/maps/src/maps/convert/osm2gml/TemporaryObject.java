package maps.convert.osm2gml;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Path2D;
import java.awt.geom.Area;
import java.awt.Shape;

import rescuecore2.misc.geometry.Point2D;
//import rescuecore2.log.Logger;

import maps.gml.GMLCoordinates;
import maps.gml.GMLTools;

/**
   Abstract base class for temporary data structures during conversion.
*/
public abstract class TemporaryObject {
    private List<DirectedEdge> edges;
    private Map<DirectedEdge, TemporaryObject> neighbours;
    private Path2D path;
    private Rectangle2D bounds;

    /**
       Construct a new TemporaryObject.
       @param edges The edges of the object in counter-clockwise order.
    */
    protected TemporaryObject(List<DirectedEdge> edges) {
        this.edges = new ArrayList<DirectedEdge>(edges);
        neighbours = new HashMap<DirectedEdge, TemporaryObject>();
    }

    /**
       Get the edges of this object.
       @return The edges.
    */
    public List<DirectedEdge> getEdges() {
        return Collections.unmodifiableList(edges);
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
        List<GMLCoordinates> result = new ArrayList<GMLCoordinates>();
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
        // Walk through the edge lists starting at the beginning for me and at the equivalent edge in other. When we reach the end of other go back to the start.
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
       Check if this object is a entirely inside another.
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
        //        Logger.debug(this + " replacing edge " + edge + " with " + replacements);
        //        Logger.debug("Old edge list: " + edges);
        if (replacements.isEmpty()) {
            // Just remove the edge
            for (Iterator<DirectedEdge> it = edges.iterator(); it.hasNext();) {
                DirectedEdge next = it.next();
                if (next.getEdge().equals(edge)) {
                    it.remove();
                }
            }
        }
        else {
            for (ListIterator<DirectedEdge> it = edges.listIterator(); it.hasNext();) {
                DirectedEdge next = it.next();
                if (next.getEdge().equals(edge)) {
                    it.remove();
                    Set<Edge> replacementsSet = new HashSet<Edge>(replacements);
                    // Create directed edges for the replacements
                    Node start = next.getStartNode();
                    Node end = next.getEndNode();
                    while (!start.equals(end)) {
                        DirectedEdge newEdge = findNewEdge(start, replacementsSet);
                        replacementsSet.remove(newEdge.getEdge());
                        it.add(newEdge);
                        start = newEdge.getEndNode();
                    }
                    break;
                }
            }
        }
        //        Logger.debug("New edge list: " + edges);
        bounds = null;
        path = null;
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
}
