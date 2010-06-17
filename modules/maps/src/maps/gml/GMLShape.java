package maps.gml;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collections;

import java.awt.geom.Rectangle2D;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.GeometryTools2D;

/**
   Abstract base class for shapes in GML space.
*/
public abstract class GMLShape extends GMLObject {
    private List<GMLDirectedEdge> edges;
    private Map<GMLDirectedEdge, Integer> neighbours;
    private List<GMLCoordinates> points;
    private Rectangle2D bounds;
    private Point2D centroid;

    /**
       Construct a GMLShape.
       @param id The ID of the shape.
    */
    protected GMLShape(int id) {
        super(id);
        this.edges = new ArrayList<GMLDirectedEdge>();
        neighbours = new HashMap<GMLDirectedEdge, Integer>();
        bounds = null;
        centroid = null;
    }

    /**
       Construct a GMLShape.
       @param id The ID of the shape.
       @param edges The edges of the shape.
    */
    protected GMLShape(int id, List<GMLDirectedEdge> edges) {
        this(id);
        this.edges.addAll(edges);
        points = getUnderlyingCoordinates();
    }

    /**
       Construct a GMLShape.
       @param id The ID of the shape.
       @param edges The edges of the shape.
       @param neighbours The neighbours of each edge.
    */
    protected GMLShape(int id, List<GMLDirectedEdge> edges, List<Integer> neighbours) {
        this(id, edges);
        Iterator<GMLDirectedEdge> it = edges.iterator();
        Iterator<Integer> ix = neighbours.iterator();
        while (it.hasNext() && ix.hasNext()) {
            setNeighbour(it.next(), ix.next());
        }
        points = getUnderlyingCoordinates();
    }

    /**
       Get the edges of this shape.
       @return The edges.
    */
    public List<GMLDirectedEdge> getEdges() {
        return new ArrayList<GMLDirectedEdge>(edges);
    }

    /**
       Set the list of edges.
       @param newEdges The new edge list.
    */
    public void setEdges(List<GMLDirectedEdge> newEdges) {
        edges.clear();
        neighbours.clear();
        edges.addAll(newEdges);
        bounds = null;
        centroid = null;
        points = getUnderlyingCoordinates();
    }

    /**
       Reorder the list of edges. This will not clear the neighbour map or the bounds.
       @param newEdges The reordered edge list.
    */
    public void reorderEdges(List<GMLDirectedEdge> newEdges) {
        edges.clear();
        edges.addAll(newEdges);
        points = getUnderlyingCoordinates();
        centroid = null;
        neighbours.keySet().retainAll(newEdges);
    }

    /**
       Replace a GMLDirectedEdge with a set of new edges. The neighbour of the old edge will be set for each of the new edges.
       @param oldEdge The edge to replace.
       @param newEdges The new edges.
    */
    public void replaceEdge(GMLDirectedEdge oldEdge, GMLDirectedEdge... newEdges) {
        ListIterator<GMLDirectedEdge> it = edges.listIterator();
        while (it.hasNext()) {
            if (it.next() == oldEdge) {
                it.remove();
                for (GMLDirectedEdge e : newEdges) {
                    it.add(e);
                }
            }
        }
        bounds = null;
        centroid = null;
        points = getUnderlyingCoordinates();
    }

    /**
       Remove an edge from this shape.
       @param edge The underlying edge to remove.
    */
    public void removeEdge(GMLEdge edge) {
        for (Iterator<GMLDirectedEdge> it = edges.iterator(); it.hasNext();) {
            GMLDirectedEdge dEdge = it.next();
            if (dEdge.getEdge().equals(edge)) {
                it.remove();
                neighbours.remove(dEdge);
            }
        }
        bounds = null;
        centroid = null;
        points = getUnderlyingCoordinates();
    }

    /**
       Get the ID of the neighbour through a particular edge.
       @param edge The edge to look up.
       @return The ID of the neighbour through that edge or null.
    */
    public Integer getNeighbour(GMLDirectedEdge edge) {
        return neighbours.get(edge);
    }

    /**
       Set the ID of the neighbour through a particular edge.
       @param edge The edge to set the neighbour of.
       @param neighbour The new neighbour ID for that edge. This may be null.
    */
    public void setNeighbour(GMLDirectedEdge edge, Integer neighbour) {
        if (neighbour == null) {
            neighbours.remove(edge);
        }
        else {
            neighbours.put(edge, neighbour);
        }
    }

    /**
       Find out if an edge has a neighbour.
       @param edge The edge to look up.
       @return True if there is a neighbour through that edge or false otherwise.
    */
    public boolean hasNeighbour(GMLDirectedEdge edge) {
        return neighbours.containsKey(edge);
    }

    /**
       Get the ID of the neighbour through a particular edge.
       @param edge The edge to look up.
       @return The ID of the neighbour through that edge or null.
    */
    public Integer getNeighbour(GMLEdge edge) {
        return getNeighbour(findDirectedEdge(edge));
    }

    /**
       Set the ID of the neighbour through a particular edge.
       @param edge The edge to set the neighbour of.
       @param neighbour The new neighbour ID for that edge. This may be null.
    */
    public void setNeighbour(GMLEdge edge, Integer neighbour) {
        setNeighbour(findDirectedEdge(edge), neighbour);
    }

    /**
       Find out if an edge has a neighbour.
       @param edge The edge to look up.
       @return True if there is a neighbour through that edge or false otherwise.
    */
    public boolean hasNeighbour(GMLEdge edge) {
        return neighbours.containsKey(findDirectedEdge(edge));
    }

    /**
       Get the coordinates of the edges that make up this shape.
       @return The underlying edge coordinates.
    */
    public List<GMLCoordinates> getUnderlyingCoordinates() {
        List<GMLCoordinates> result = new ArrayList<GMLCoordinates>();
        for (GMLDirectedEdge next : edges) {
            result.add(next.getStartCoordinates());
        }
        return result;
    }

    /**
    Get the nodes of the edges that make up this shape.
    @return The underlying nodes.
     */
    public List<GMLNode> getUnderlyingNodes() {
     List<GMLNode> result = new ArrayList<GMLNode>();
     for (GMLDirectedEdge next : edges) {
         result.add(next.getStartNode());
     }
     return result;
 }

    /**
       Get the coordinates of the apexes of this shape.
       @return The apex coordinates.
    */
    public List<GMLCoordinates> getCoordinates() {
        return Collections.unmodifiableList(points);
    }

    /**
       Set the coordinates of the apexes of this shape.
       @param newPoints The new apex coordinates.
     */
    public void setCoordinates(List<GMLCoordinates> newPoints) {
        points.clear();
        points.addAll(newPoints);
        bounds = null;
        centroid = null;
    }

    /**
       Get the x coordinate of the centroid of this shape.
       @return The x coordinate of the centroid.
    */
    public double getCentreX() {
        return getCentroid().getX();
    }

    /**
       Get the y coordinate of the centroid of this shape.
       @return The y coordinate of the centroid.
    */
    public double getCentreY() {
        return getCentroid().getY();
    }

    /**
       Get the bounds of this shape.
       @return The bounds of the shape.
    */
    public Rectangle2D getBounds() {
        if (bounds == null) {
            bounds = GMLTools.getBounds(getCoordinates());
        }
        return bounds;
    }

    /**
       Get the centroid of this shape.
       @return The centroid of the shape.
    */
    public Point2D getCentroid() {
        if (centroid == null) {
            centroid = GeometryTools2D.computeCentroid(GMLTools.coordinatesAsPoints(getCoordinates()));
        }
        return centroid;
    }

    private GMLDirectedEdge findDirectedEdge(GMLEdge e) {
        for (GMLDirectedEdge next : edges) {
            if (next.getEdge().equals(e)) {
                return next;
            }
        }
        throw new IllegalArgumentException(this + ": Edge " + e + " not found");
    }
}
