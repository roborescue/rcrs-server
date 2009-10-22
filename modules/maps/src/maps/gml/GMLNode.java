package maps.gml;

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
       Get the X (longitude) coordinate.
       @return The longitude coordinate.
     */
    public double getX() {
        return coordinates.getX();
    }

    /**
       Get the Y (latitude) coordinate.
       @return The latitude coordinate.
     */
    public double getY() {
        return coordinates.getY();
    }

    @Override
    public String toString() {
        return "GMLNode " + getID() + " at " + coordinates;
    }
}