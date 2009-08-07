package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.geom.Line2D;

import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Node;
import rescuecore2.standard.entities.StandardWorldModel;

/**
   A view layer that renders roads.
 */
public class RoadLayer extends StandardEntityViewLayer<Road> {
    private static final int LANE_WIDTH = 2;

    /**
       Construct a road rendering layer.
     */
    public RoadLayer() {
        super(Road.class);
    }

    @Override
    public Shape render(Road r, Graphics2D g, ScreenTransform t, StandardWorldModel model) {
        Node head = (Node)r.getHead(model);
        Node tail = (Node)r.getTail(model);
        int headX = t.scaleX(head.getX());
        int headY = t.scaleY(head.getY());
        int tailX = t.scaleX(tail.getX());
        int tailY = t.scaleY(tail.getY());
        int lanes = r.getLinesToHead(); // Assume symmetric road
        int lanesBlocked = r.countBlockedLanes();
        if (lanesBlocked == 0) {
            g.setColor(Color.LIGHT_GRAY);
        }
        else if (lanesBlocked < lanes) {
            g.setColor(Color.GRAY);
        }
        else if (lanesBlocked == lanes) {
            g.setColor(Color.DARK_GRAY);
        }
        else {
            System.err.println("Lane block calculation screwed up: " + r + " reported " + lanesBlocked + " blocked lanes.");
        }
        Line2D line = new Line2D.Double(headX, headY, tailX, tailY);
        Shape shape = new BasicStroke(lanes * LANE_WIDTH).createStrokedShape(line);
        g.fill(shape);
        return shape;
    }
}