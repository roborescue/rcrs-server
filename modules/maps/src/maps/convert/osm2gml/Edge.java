package maps.convert.osm2gml;

import lombok.Getter;
import maps.convert.osm2gml.debug.Lineal;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;

import java.awt.geom.Rectangle2D;

/**
 * An edge. An edge is a line between two nodes.
 */
@Getter
public class Edge extends ManagedObject implements SpatialIndexable, Lineal {
    private final Node start;
    private final Node end;
    private final Line2D line;

    /**
     * Construct a new edge.
     * @param id    The ID of this object.
     * @param start The start node.
     * @param end   The end node.
     */
    public Edge(long id, Node start, Node end) {
        super(id);
        this.start = start;
        this.end = end;
        line = new Line2D(start.getPoint(), end.getPoint());
    }

    /**
     * Get the midpoint of this edge.
     * @return A new {@link Point2D} representing the midpoint.
     */
    public Point2D getMidpoint() {
        return line.getMidpoint();
    }

    @Override
    public String toString() {
        return "Edge#" + getID() + " (start=" + start + ", end=" + end + ")";
    }

    @Override
    public Rectangle2D getBounds() {
        final Point2D p1 = start.getPoint();
        final Point2D p2 = end.getPoint();
        final double x = Math.min(p1.getX(), p2.getX());
        final double y = Math.min(p1.getY(), p2.getY());
        final double width = Math.abs(p1.getX() - p2.getX());
        final double height = Math.abs(p1.getY() - p2.getY());
        return new Rectangle2D.Double(x, y, width, height);
    }
}
