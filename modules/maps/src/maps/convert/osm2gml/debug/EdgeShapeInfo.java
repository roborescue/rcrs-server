package maps.convert.osm2gml.debug;

import java.awt.Color;

import maps.convert.osm2gml.Edge;
import rescuecore2.misc.gui.ShapeDebugFrame;

/**
 * A {@link rescuecore2.misc.gui.ShapeDebugFrame.Line2DShapeInfo} for debugging a {@link Edge}.
 */
public class EdgeShapeInfo extends ShapeDebugFrame.Line2DShapeInfo {
    /**
     * Create a new {@code EdgeShapeInfo} object.
     * @param edge  The edge to display.
     * @param name  The name of the edge.
     * @param color The color of the edge.
     * @param thick Whether to draw the edge thick or not.
     * @param arrow Whether to draw the edge's direction or not.
     */
    public EdgeShapeInfo(final Edge edge, final String name, final Color color,
                         final boolean thick, final boolean arrow)  {
        super(edge.getLine(), name, color, thick, arrow);
    }
}
