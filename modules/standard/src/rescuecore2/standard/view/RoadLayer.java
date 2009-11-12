package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.geom.Line2D;

import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Node;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A view layer that renders roads.
 */
public class RoadLayer extends StandardEntityViewLayer<Road> {
    private static final int ROAD_WIDTH = 2;
    private static final int LANE_WIDTH = 1;

    private static final Color ROAD_COLOUR = new Color(185, 185, 185);

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
    public Shape render(Road r, Graphics2D g, ScreenTransform t) {
        Node head = (Node)r.getHead(world);
        Node tail = (Node)r.getTail(world);
        int headX = t.xToScreen(head.getX());
        int headY = t.yToScreen(head.getY());
        int tailX = t.xToScreen(tail.getX());
        int tailY = t.yToScreen(tail.getY());
        int lanes = r.getLinesToHead() * 2; // Assume symmetric road
        g.setColor(ROAD_COLOUR);
        Line2D line = new Line2D.Double(headX, headY, tailX, tailY);
        Shape shape = new BasicStroke(ROAD_WIDTH + (lanes * LANE_WIDTH)).createStrokedShape(line);
        g.fill(shape);
        return shape;
    }
}