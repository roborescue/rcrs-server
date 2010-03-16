package maps.convert.osm2gml;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;

/**
   A DirectedEdge is an edge with an orientation.
 */
public class DirectedEdge {
    private Edge edge;
    private boolean forward;
    private Line2D line;

    /**
       Construct a directed edge.
       @param edge The underlying edge.
       @param forward True if this directed edge is aligned with the underlying edge direction, false otherwise.
     */
    public DirectedEdge(Edge edge, boolean forward) {
        this.edge = edge;
        this.forward = forward;
        this.line = edge.getLine();
        if (!forward) {
            line = new Line2D(line.getEndPoint(), line.getOrigin());
        }
    }

    /**
       Construct a directed edge.
       @param edge The underlying edge.
       @param start The start node.
     */
    public DirectedEdge(Edge edge, Node start) {
        this.edge = edge;
        this.forward = start.equals(edge.getStart());
        this.line = edge.getLine();
        if (!forward) {
            line = new Line2D(line.getEndPoint(), line.getOrigin());
        }
    }

    /**
       Get the underlying edge.
       @return The underlying edge.
     */
    public Edge getEdge() {
        return edge;
    }

    /**
       Get the line represented by this edge.
       @return The line.
    */
    public Line2D getLine() {
        return line;
    }

    /**
       Is this directed edge in the direction of the underlying edge?
       @return True if this directed edge is aligned with the underlying edge direction, false otherwise.
     */
    public boolean isForward() {
        return forward;
    }

    /**
       Get the node at the start of the underlying edge.
       @return The start node.
     */
    public Node getStartNode() {
        return forward ? edge.getStart() : edge.getEnd();
    }

    /**
       Get the node at the end of the underlying edge.
       @return The end node.
     */
    public Node getEndNode() {
        return forward ? edge.getEnd() : edge.getStart();
    }

    /**
       Get the coordinates of the start of this edge.
       @return The coordinates of the start of this edge.
     */
    public Point2D getStartCoordinates() {
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
    public Point2D getEndCoordinates() {
        if (forward) {
            return edge.getEnd().getCoordinates();
        }
        else {
            return edge.getStart().getCoordinates();
        }
    }

    @Override
    public String toString() {
        return "DirectedEdge" + (forward ? "" : " backwards") + " along " + edge;
    }

    @Override
    public int hashCode() {
        return edge.hashCode() ^ (forward ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DirectedEdge) {
            DirectedEdge e = (DirectedEdge)o;
            return this.forward == e.forward && this.edge.equals(e.edge);
        }
        return false;
    }
}
