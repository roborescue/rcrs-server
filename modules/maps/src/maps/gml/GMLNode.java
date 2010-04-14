package maps.gml;

import maps.CoordinateConversion;

/**
   A GML node object.
 */
public class GMLNode extends GMLObject {
    private GMLCoordinates coordinates;

    /**
       Construct a new GML node.
       @param id The ID of this node.
       @param x The x coordinate of this node.
       @param y The y coordinate of this node.
     */
    public GMLNode(int id, double x, double y) {
        this(id, new GMLCoordinates(x, y));
    }

    /**
       Construct a new GML node.
       @param id The ID of this node.
       @param coordinates The coordinates of this node.
     */
    public GMLNode(int id, GMLCoordinates coordinates) {
        super(id);
        this.coordinates = coordinates;
    }

    /**
       Get the coordinates of this node.
       @return The node coordinates.
     */
    public GMLCoordinates getCoordinates() {
        return coordinates;
    }

    /**
       Set the coordinates of this node.
       @param c The new coordinates.
    */
    public void setCoordinates(GMLCoordinates c) {
        if (c == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        this.coordinates = c;
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

    /**
       Apply a CoordinateConversion to this node.
       @param c The conversion to apply.
    */
    public void convert(CoordinateConversion c) {
        double oldX = coordinates.getX();
        double oldY = coordinates.getY();
        double newX = c.convertX(oldX);
        double newY = c.convertY(oldY);
        coordinates = new GMLCoordinates(newX, newY);
    }

    @Override
    public String toString() {
        return "GMLNode " + getID() + " at " + coordinates;
    }
}
