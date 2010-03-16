package maps.convert.osm2gml;

import java.awt.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import rescuecore2.misc.gui.ShapeDebugFrame;

import rescuecore2.misc.geometry.Line2D;

/**
   A ShapeInfo that knows how to draw Edges.
*/
public class EdgeShapeInfo extends ShapeDebugFrame.Line2DShapeInfo {
    private Collection<Edge> edges;

    /**
       Create a new EdgeShapeInfo.
       @param edge The edge to draw.
       @param name The name of the edge.
       @param colour The colour to draw the edge.
       @param thick Whether to draw the edge thick or not.
       @param arrow Whether to draw the edge's direction or not.
     */
    public EdgeShapeInfo(Edge edge, String name, Color colour, boolean thick, boolean arrow)  {
        this(Collections.singleton(edge), name, colour, thick, arrow);
    }

    /**
       Create a new EdgeShapeInfo.
       @param edges The edges to draw.
       @param name The name of the edge.
       @param colour The colour to draw the edge.
       @param thick Whether to draw the edge thick or not.
       @param arrow Whether to draw the edge's direction or not.
     */
    public EdgeShapeInfo(Collection<Edge> edges, String name, Color colour, boolean thick, boolean arrow)  {
        super(makeLines(edges), name, colour, thick, arrow);
        this.edges = edges;
    }

    @Override
    public Object getObject() {
        return edges;
    }

    private static Collection<Line2D> makeLines(Collection<Edge> edges) {
        if (edges == null) {
            return null;
        }
        Collection<Line2D> result = new ArrayList<Line2D>();
        for (Edge next : edges) {
            result.add(next.getLine());
        }
        return result;
    }
}
