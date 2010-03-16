package maps.convert.osm2gml;

import rescuecore2.misc.geometry.Point2D;

/**
   A node object.
 */
public class Node extends ManagedObject {
    private Point2D coordinates;

    /**
       Construct a new node.
       @param id The ID of this node.
       @param x The x coordinate of this node.
       @param y The y coordinate of this node.
     */
    public Node(long id, double x, double y) {
        this(id, new Point2D(x, y));
    }

    /**
       Construct a new node.
       @param id The ID of this node.
       @param coordinates The coordinates of this node.
     */
    public Node(long id, Point2D coordinates) {
        super(id);
        this.coordinates = coordinates;
    }

    /**
       Get the coordinates of this node.
       @return The node coordinates.
     */
    public Point2D getCoordinates() {
        return coordinates;
    }

    /**
       Get the X coordinate.
       @return The X coordinate.
     */
    public double getX() {
        return coordinates.getX();
    }

    /**
       Get the Y coordinate.
       @return The Y coordinate.
     */
    public double getY() {
        return coordinates.getY();
    }

    @Override
    public String toString() {
        return "Node " + getID() + " at " + coordinates;
    }
}
