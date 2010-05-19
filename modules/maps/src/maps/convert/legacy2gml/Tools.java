package maps.convert.legacy2gml;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;

import maps.gml.GMLNode;
import maps.gml.GMLDirectedEdge;

/**
   Utilities for the legacy2gml covertor.
*/
public final class Tools {
    private Tools() {}

    /**
       Convert a list of GML nodes to Line2D objects.
       @param nodes The node list.
       @return A new Line2D list.
    */
    public static List<Line2D> nodeListToLineList(List<GMLNode> nodes) {
        List<Line2D> result = new ArrayList<Line2D>(nodes.size());
        Iterator<GMLNode> it = nodes.iterator();
        GMLNode first = it.next();
        GMLNode prev = first;
        while (it.hasNext()) {
            GMLNode next = it.next();
            result.add(new Line2D(new Point2D(prev.getX(), prev.getY()), new Point2D(next.getX(), next.getY())));
            prev = next;
        }
        result.add(new Line2D(new Point2D(prev.getX(), prev.getY()), new Point2D(first.getX(), first.getY())));
        return result;
    }

    /**
       Convert a GMLDirectedEdge to a Line2D.
       @param edge The edge to convert.
       @return A new Line2D.
    */
    public static Line2D gmlDirectedEdgeToLine(GMLDirectedEdge edge) {
        GMLNode start = edge.getStartNode();
        GMLNode end = edge.getEndNode();
        Point2D origin = new Point2D(start.getX(), start.getY());
        Point2D endPoint = new Point2D(end.getX(), end.getY());
        return new Line2D(origin, endPoint);
    }
}