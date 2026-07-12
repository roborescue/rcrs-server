package maps.convert.osm2gml.debug;

import java.awt.Color;

import rescuecore2.misc.gui.ShapeDebugFrame;

public class LineShapeInfo extends ShapeDebugFrame.Line2DShapeInfo {
    public LineShapeInfo(final Lineal line, final String name, final Color color,
                         final boolean thick, final boolean arrow)  {
        super(line.getLine(), name, color, thick, arrow);
    }
}
