package maps.gml;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

/**
   A GMLFace is a set of directed edges, an optional outline polygon and an optional type.
 */
public class GMLFace extends GMLObject {
    private List<GMLDirectedEdge> edges;
    private List<GMLCoordinates> points;

    /**
       Construct a new GMLFace that calculates it's own outline shape from the directed edges.
       @param id The ID of the face.
       @param edges The edges of the face.
    */
    public GMLFace(int id, List<GMLDirectedEdge> edges) {
        this(id, edges, null);
    }

    /**
       Construct a new GMLFace.
       @param id The ID of the face.
       @param edges The edges of the face.
       @param points The outline of the face. If this is null or empty then the outline will be calculated based on the directed edges.
    */
    public GMLFace(int id, List<GMLDirectedEdge> edges, List<GMLCoordinates> points) {
        super(id);
        this.edges = edges;
        this.points = new ArrayList<GMLCoordinates>();
        if (points == null || points.isEmpty()) {
            computeOutline();
        }
        else {
            this.points.addAll(points);
        }
    }

    /**
       Get the list of edges.
       @return The edges of this face.
    */
    public List<GMLDirectedEdge> getEdges() {
        return new ArrayList<GMLDirectedEdge>(edges);
    }

    /**
       Find out if this face is connected to a particular edge.
       @param edge The edge to check.
       @return true iff this face has a directed edge connected to the given edge.
    */
    public boolean isConnectedTo(GMLEdge edge) {
        for (GMLDirectedEdge next : edges) {
            if (next.getEdge() == edge) {
                return true;
            }
        }
        return false;
    }

    /**
       Find out if this face is connected to a particular edge on the left-hand side, i.e. in a "forward" direction along the edge.
       @param edge The edge to check.
       @return true iff this face has a directed edge connected to the given edge on the left-hand side.
    */
    public boolean isConnectedLeft(GMLEdge edge) {
        for (GMLDirectedEdge next : edges) {
            if (next.getEdge() == edge && next.isForward()) {
                return true;
            }
        }
        return false;
    }

    /**
       Find out if this face is connected to a particular edge on the right-hand side, i.e. in a "backward" direction along the edge.
       @param edge The edge to check.
       @return true iff this face has a directed edge connected to the given edge on the right-hand side.
    */
    public boolean isConnectedRight(GMLEdge edge) {
        for (GMLDirectedEdge next : edges) {
            if (next.getEdge() == edge && !next.isForward()) {
                return true;
            }
        }
        return false;
    }

    /**
       Get the points of the outline of this face.
       @return The outline coordinates.
    */
    public List<GMLCoordinates> getPoints() {
        return Collections.unmodifiableList(points);
    }

    /**
       Set the points of the outline of this face.
       @param newPoints The new outline coordinates.
     */
    public void setPoints(List<GMLCoordinates> newPoints) {
        points.clear();
        points.addAll(newPoints);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("GMLFace [");
        for (Iterator<GMLDirectedEdge> it = edges.iterator(); it.hasNext();) {
            result.append(it.next());
            if (it.hasNext()) {
                result.append(", ");
            }
        }
        result.append("]");
        return result.toString();
    }

    private void computeOutline() {
        points.clear();
        Iterator<GMLDirectedEdge> it = edges.iterator();
        GMLDirectedEdge next = it.next();
        points.add(next.getStartNode().getCoordinates());
        points.add(next.getEndNode().getCoordinates());
        while (it.hasNext()) {
            next = it.next();
            points.add(next.getEndNode().getCoordinates());
        }
    }
}