package maps.convert.osm2gml;

import maps.convert.osm2gml.debug.Puntal;
import rescuecore2.misc.geometry.Point2D;

/**
 * A node object.
 */
public class Node extends ManagedObject implements Puntal {
    private final Point2D point;

    /**
     * Construct a new node.
     * @param id The ID of this node.
     * @param x  The x coordinate of this node.
     * @param y  The y coordinate of this node.
     */
    public Node(final long id, final double x, final double y) {
        this(id, new Point2D(x, y));
    }

    /**
     * Construct a new node.
     * @param id    The ID of this node.
     * @param point The coordinates of this node.
     */
    public Node(final long id, final Point2D point) {
        super(id);
        this.point = point;
    }

    @Override
    public Point2D getPoint() {
        return point;
    }

    /**
     * Get the X coordinate.
     * @return The X coordinate.
     */
    public double getX() {
        return point.getX();
    }

    /**
     * Get the Y coordinate.
     * @return The Y coordinate.
     */
    public double getY() {
        return point.getY();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + getID() + "(point=" + point + ")";
    }
}
