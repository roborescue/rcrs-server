package maps.gml;

/**
   A set of GML coordinates. These coordinates can be any unit.
*/
public class GMLCoordinates {
    private double x;
    private double y;

    /**
       Create a new GMLCoordinates object.
       @param x The X coordinate.
       @param y The Y coordinate.
     */
    public GMLCoordinates(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
       Create a new GMLCoordinates object from a String of the form "x,y".
       @param s The String to read.
       @throws IllegalArgumentException If the string is invalid.
     */
    public GMLCoordinates(String s) {
        int index = s.indexOf(",");
        if (index == -1) {
            throw new IllegalArgumentException("'" + s + "' is not of the form 'x,y'");
        }
        try {
            this.x = Double.parseDouble(s.substring(0, index));
            this.y = Double.parseDouble(s.substring(index + 1));
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + s + "' is not of the form 'x,y'", e);
        }
    }

    /**
       Get the X coordinate.
       @return The X coordinate.
     */
    public double getX() {
        return x;
    }

    /**
       Get the Y coordinate.
       @return The y coordinate.
     */
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(x);
        result.append(",");
        result.append(y);
        return result.toString();
    }
}