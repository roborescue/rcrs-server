package maps.gml.debug;

import java.awt.Color;

import rescuecore2.misc.gui.ShapeDebugFrame;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;

import maps.gml.GMLDirectedEdge;
import maps.gml.GMLNode;

/**
   A ShapeInfo that knows how to draw GMLDirectedEdges.
*/
public class GMLDirectedEdgeShapeInfo extends ShapeDebugFrame.Line2DShapeInfo {
    private GMLDirectedEdge edge;

    /**
       Create a new GMLDirectedEdgeShapeInfo.
       @param edge The directed edge to draw.
       @param name The name of the edge.
       @param colour The colour to draw the edge.
       @param thick Whether to draw the edge thick or not.
       @param arrow Whether to draw the direction arrow or not.
     */
    public GMLDirectedEdgeShapeInfo(GMLDirectedEdge edge, String name, Color colour, boolean thick, boolean arrow)  {
        super(gmlDirectedEdgeToLine(edge), name, colour, thick, arrow);
        this.edge = edge;
    }

    @Override
    public Object getObject() {
        return edge;
    }

    private static Line2D gmlDirectedEdgeToLine(GMLDirectedEdge edge) {
        if (edge == null) {
            return null;
        }
        GMLNode start = edge.getStartNode();
        GMLNode end = edge.getEndNode();
        Point2D origin = new Point2D(start.getX(), start.getY());
        Point2D endPoint = new Point2D(end.getX(), end.getY());
        return new Line2D(origin, endPoint);
    }
}
