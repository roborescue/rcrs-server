package maps.convert.osm2gml;

import lombok.Getter;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;

/**
   A DirectedEdge is an edge with an orientation.
 */
@Getter
public class DirectedEdge {
    private final Edge edge;
    private final boolean forward;
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
     * Returns a new DirectedEdge that represents the same underlying edge
     * but traversed in the opposite direction.
     * @return A new DirectedEdge with the reverse direction.
     */
    public DirectedEdge getReverse() {
        return new DirectedEdge(this.edge, !this.forward);
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

    /**
     * Get the length of this directed edge.
     * @return The length of the directed edge.
     */
    public double getLength() {
        return line.getDirection().getLength();
    }

    /**
     * Get the midpoint of this directed edge.
     * @return A new {@link Point2D} representing the midpoint.
     */
    public Point2D getMidpoint() {
        return edge.getMidpoint();
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
        if (o instanceof DirectedEdge e) {
            return this.forward == e.forward && this.edge.equals(e.edge);
        }
        return false;
    }
}
