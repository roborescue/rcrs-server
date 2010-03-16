package maps.gml;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import java.awt.geom.Rectangle2D;

import rescuecore2.misc.geometry.Point2D;

/**
   Useful tools for manipulating GML.
*/
public final class GMLTools {
    private GMLTools() {
    }

    /**
       Turn a list of coordinates into a string suitable for putting into an XML document.
       @param coords The coordinate list.
       @return A string version of the list.
    */
    public static String getCoordinatesString(List<GMLCoordinates> coords) {
        StringBuilder result = new StringBuilder();
        for (Iterator<GMLCoordinates> it = coords.iterator(); it.hasNext();) {
            GMLCoordinates next = it.next();
            result.append(String.valueOf(next.getX()));
            result.append(",");
            result.append(String.valueOf(next.getY()));
            if (it.hasNext()) {
                result.append(" ");
            }
        }
        return result.toString();
    }

    /**
       Turn a coordinates string into a list of GMLCoordinates.
       @param coords The coordinates string.
       @return A list of GMLCoordinates.
    */
    public static List<GMLCoordinates> getCoordinatesList(String coords) {
        List<GMLCoordinates> result = new ArrayList<GMLCoordinates>();
        StringTokenizer tokens = new StringTokenizer(coords, " ");
        while (tokens.hasMoreTokens()) {
            result.add(new GMLCoordinates(tokens.nextToken()));
        }
        return result;
    }

    /**
       Convert a list of GMLCoordinates to Point2D objects.
       @param coords The GMLCoordinates to convert.
       @return A list of Point2D objects.
    */
    public static List<Point2D> coordinatesAsPoints(List<GMLCoordinates> coords) {
        List<Point2D> result = new ArrayList<Point2D>(coords.size());
        for (GMLCoordinates next : coords) {
            result.add(new Point2D(next.getX(), next.getY()));
        }
        return result;
    }

    /**
       Get the bounds of a set of coordinates.
       @param coords The coordinate list.
       @return The bounds of the coordinates.
    */
    public static Rectangle2D getBounds(List<GMLCoordinates> coords) {
        if (coords.isEmpty()) {
            return null;
        }
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (GMLCoordinates next : coords) {
            minX = Math.min(minX, next.getX());
            minY = Math.min(minY, next.getY());
            maxX = Math.max(maxX, next.getX());
            maxY = Math.max(maxY, next.getY());
        }
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }
}
