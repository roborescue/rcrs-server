package maps.gml;

import java.util.List;
import java.util.ArrayList;

/**
   A GMLDirectedEdge is an edge with an orientation.
 */
public class GMLDirectedEdge {
    private GMLEdge edge;
    private boolean forward;

    /**
       Construct a directed GML edge.
       @param edge The underlying edge.
       @param forward True if this directed edge is aligned with the underlying edge direction, false otherwise.
     */
    public GMLDirectedEdge(GMLEdge edge, boolean forward) {
        this.edge = edge;
        this.forward = forward;
    }

    /**
       Construct a directed GML edge.
       @param edge The underlying edge.
       @param start The start node.
     */
    public GMLDirectedEdge(GMLEdge edge, GMLNode start) {
        this.edge = edge;
        this.forward = start.equals(edge.getStart());
    }

    /**
       Get the underlying edge.
       @return The underlying edge.
     */
    public GMLEdge getEdge() {
        return edge;
    }

    /**
       Is this directed edge in the direction of the underlying edge?
       @return True if this directed edge is aligned with the underlying edge direction, false otherwise.
     */
    public boolean isForward() {
        return forward;
    }

    /**
       Reverse the direction of this edge.
    */
    public void reverse() {
        forward = !forward;
    }

    /**
       Get the node at the start of the underlying edge.
       @return The start node.
     */
    public GMLNode getStartNode() {
        return forward ? edge.getStart() : edge.getEnd();
    }

    /**
       Get the node at the end of the underlying edge.
       @return The end node.
     */
    public GMLNode getEndNode() {
        return forward ? edge.getEnd() : edge.getStart();
    }

    /**
       Get the points of the underlying edge in the right order for this directed edge.
       @return The points of the underlying edge in the right order.
    */
    public List<GMLCoordinates> getPoints() {
        List<GMLCoordinates> result = new ArrayList<GMLCoordinates>();
        result.add(getStartNode().getCoordinates());
        result.add(getEndNode().getCoordinates());
        return result;
    }

    /**
       Get the coordinates of the start of this edge.
       @return The coordinates of the start of this edge.
     */
    public GMLCoordinates getStartCoordinates() {
        if (forward) {
            return edge.getStart().getCoordinates();
        }
        else {
            return edge.getEnd().getCoordinates();
        }
    }

    /**
       Get the coordinates of the end of this edge.
       @return The coordinates of the end of this edge.
     */
    public GMLCoordinates getEndCoordinates() {
        if (forward) {
            return edge.getEnd().getCoordinates();
        }
        else {
            return edge.getStart().getCoordinates();
        }
    }

    @Override
    public String toString() {
        return "GMLDirectedEdge" + (forward ? "" : " backwards") + " along " + edge;
    }

    @Override
    public int hashCode() {
        return edge.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GMLDirectedEdge) {
            GMLDirectedEdge e = (GMLDirectedEdge)o;
            return this.forward == e.forward && this.edge.equals(e.edge);
        }
        return false;
    }
}
