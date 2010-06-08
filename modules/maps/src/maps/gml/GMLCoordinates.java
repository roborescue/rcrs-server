package maps.gml;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
   A set of GML coordinates. These coordinates are in m.
*/
public class GMLCoordinates {
    private static final NumberFormat FORMAT = new DecimalFormat("#0.000", DecimalFormatSymbols.getInstance(Locale.US));

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
       Copy constructor.
       @param other The GMLCoordinates to copy.
     */
    public GMLCoordinates(GMLCoordinates other) {
        this.x = other.x;
        this.y = other.y;
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

    /**
       Set the X coordinate.
       @param newX The new X coordinate.
     */
    public void setX(double newX) {
        x = newX;
    }

    /**
       Set the Y coordinate.
       @param newY The new Y coordinate.
     */
    public void setY(double newY) {
        y = newY;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(FORMAT.format(x));
        result.append(",");
        result.append(FORMAT.format(y));
        return result.toString();
    }
}
