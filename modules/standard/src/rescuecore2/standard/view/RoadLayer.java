package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Polygon;

import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Edge;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A view layer that renders roads.
 */
public class RoadLayer extends AreaLayer<Road> {
    private static final Color ROAD_EDGE_COLOUR = Color.GRAY.darker();
    private static final Color ROAD_SHAPE_COLOUR = new Color(185, 185, 185);

    private static final Stroke WALL_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final Stroke ENTRANCE_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    /**
       Construct a road rendering layer.
     */
    public RoadLayer() {
        super(Road.class);
    }

    @Override
    public String getName() {
        return "Roads";
    }

    @Override
    protected void paintShape(Road r, Polygon shape, Graphics2D g) {
        g.setColor(ROAD_SHAPE_COLOUR);
        g.fill(shape);
    }

    @Override
    protected void paintEdge(Edge e, Graphics2D g, ScreenTransform t) {
        g.setColor(ROAD_EDGE_COLOUR);
        g.setStroke(e.isPassable() ? ENTRANCE_STROKE : WALL_STROKE);
        g.drawLine(t.xToScreen(e.getStartX()),
                   t.yToScreen(e.getStartY()),
                   t.xToScreen(e.getEndX()),
                   t.yToScreen(e.getEndY()));
    }
}
