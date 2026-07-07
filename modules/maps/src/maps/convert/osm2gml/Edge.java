package maps.convert.osm2gml;

import lombok.Getter;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;

import java.awt.geom.Rectangle2D;

/**
   An edge. An edge is a line between two nodes.
 */
@Getter
public class Edge extends ManagedObject implements SpatialIndexable{
    private final Node start;
    private final Node end;
    private final Line2D line;

    /**
       Construct a new Edge.
       @param id The ID of this object.
       @param start The start node.
       @param end The end node.
     */
    public Edge(long id, Node start, Node end) {
        super(id);
        this.start = start;
        this.end = end;
        line = new Line2D(start.getCoordinates(), end.getCoordinates());
    }

    /**
     * Get the midpoint of this edge.
     * @return A new {@link Point2D} representing the midpoint.
     */
    public Point2D getMidpoint() {
        final Point2D p1 = start.getCoordinates();
        final Point2D p2 = end.getCoordinates();
        return new Point2D((p1.getX() + p2.getX()) / 2.0, (p1.getY() + p2.getY()) / 2.0);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Edge ");
        result.append(getID());
        result.append(" from ");
        result.append(start);
        result.append(" to ");
        result.append(end);
        return result.toString();
    }

    @Override
    public Rectangle2D getBounds() {
        Point2D p1 = start.getCoordinates();
        Point2D p2 = end.getCoordinates();
        double x = Math.min(p1.getX(), p2.getX());
        double y = Math.min(p1.getY(), p2.getY());
        double width = Math.abs(p1.getX() - p2.getX());
        double height = Math.abs(p1.getY() - p2.getY());
        return new Rectangle2D.Double(x, y, width, height);
    }
}
