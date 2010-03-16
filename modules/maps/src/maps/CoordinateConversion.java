package maps;

/**
   Interface for converting coordinates from one system to another.
*/
public interface CoordinateConversion {
    /**
       Convert an X coordinate.
       @param x The coordinate to convert.
       @return The converted coordinate.
    */
    double convertX(double x);

    /**
       Convert a Y coordinate.
       @param y The coordinate to convert.
       @return The converted coordinate.
    */
    double convertY(double y);
}
