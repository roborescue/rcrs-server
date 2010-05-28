package maps.gml;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Path2D;
import java.awt.Shape;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;

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

    /**
       Get the bounds of a set of gml objects.
       @param objects The object list.
       @return The bounds of the objects.
    */
    public static Rectangle2D getObjectBounds(List<? extends GMLObject> objects) {
        Rectangle2D result = null;
        for (GMLObject next : objects) {
            result = expand(result, next);
        }
        return result;
    }

    /**
       Turn a list of coordinates into a shape.
       @param coords The coordinates.
       @return A new shape.
    */
    public static Shape coordsToShape(List<GMLCoordinates> coords) {
        Path2D path = new Path2D.Double();
        Iterator<GMLCoordinates> it = coords.iterator();
        GMLCoordinates c = it.next();
        path.moveTo(c.getX(), c.getY());
        while (it.hasNext()) {
            c = it.next();
            path.lineTo(c.getX(), c.getY());
        }
        path.closePath();
        return path;
    }

    /**
       Turn a GMLNode into a Point2D.
       @param node The node to convert.
       @return A new Point2D.
    */
    public static Point2D toPoint(GMLNode node) {
        return new Point2D(node.getX(), node.getY());
    }

    /**
       Turn a GMLEdge into a Line2D.
       @param edge The edge to convert.
       @return A new Line2D.
    */
    public static Line2D toLine(GMLEdge edge) {
        return new Line2D(toPoint(edge.getStart()), toPoint(edge.getEnd()));
    }

    private static Rectangle2D expand(Rectangle2D rect, double x, double y) {
        if (rect == null) {
            return new Rectangle2D.Double(x, y, 0, 0);
        }
        double newMinX = Math.min(x, rect.getX());
        double newMaxX = Math.max(x, rect.getX() + rect.getWidth());
        double newMinY = Math.min(y, rect.getY());
        double newMaxY = Math.max(y, rect.getY() + rect.getHeight());
        rect.setRect(newMinX, newMinY, newMaxX - newMinX, newMaxY - newMinY);
        return rect;
    }

    private static Rectangle2D expand(Rectangle2D rect, GMLNode node) {
        return expand(rect, node.getX(), node.getY());
    }

    private static Rectangle2D expand(Rectangle2D rect, GMLEdge edge) {
        return expand(expand(rect, edge.getStart()), edge.getEnd());
    }

    private static Rectangle2D expand(Rectangle2D rect, GMLShape shape) {
        for (GMLDirectedEdge next : shape.getEdges()) {
            rect = expand(rect, next.getEdge());
        }
        return rect;
    }

    private static Rectangle2D expand(Rectangle2D rect, GMLObject object) {
        if (object instanceof GMLNode) {
            return expand(rect, (GMLNode)object);
        }
        if (object instanceof GMLEdge) {
            return expand(rect, (GMLEdge)object);
        }
        if (object instanceof GMLShape) {
            return expand(rect, (GMLShape)object);
        }
        return rect;
    }
}
