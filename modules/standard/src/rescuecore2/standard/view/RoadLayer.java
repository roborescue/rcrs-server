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
    private static final int LANE_WIDTH = 2;
    private static final int BLOCK_SIZE = 4;
    private static final int BLOCK_STROKE_WIDTH = 2;

    private static final Color ROAD_COLOUR = new Color(185, 185, 185);
    private static final Color PARTIAL_BLOCK_COLOUR = Color.gray.darker();
    private static final Color TOTAL_BLOCK_COLOUR = Color.black;

    /**
       Construct a road rendering layer.
     */
    public RoadLayer() {
        super(Road.class);
    }

    @Override
    public Shape render(Road r, Graphics2D g, ScreenTransform t) {
        Node head = (Node)r.getHead(world);
        Node tail = (Node)r.getTail(world);
        int headX = t.xToScreen(head.getX());
        int headY = t.yToScreen(head.getY());
        int tailX = t.xToScreen(tail.getX());
        int tailY = t.yToScreen(tail.getY());
        int lanes = r.getLinesToHead(); // Assume symmetric road
        g.setColor(ROAD_COLOUR);
        Line2D line = new Line2D.Double(headX, headY, tailX, tailY);
        Shape shape = new BasicStroke(lanes * LANE_WIDTH).createStrokedShape(line);
        g.fill(shape);
        // Draw the block
        int lanesBlocked = r.countBlockedLanes();
        if (lanesBlocked != 0) {
            g.setColor(lanesBlocked == lanes ? TOTAL_BLOCK_COLOUR : PARTIAL_BLOCK_COLOUR);
            g.setStroke(new BasicStroke(BLOCK_STROKE_WIDTH));
            int x = (headX + tailX) / 2;
            int y = (headY + tailY) / 2;
            g.drawLine(x - BLOCK_SIZE, y - BLOCK_SIZE, x + BLOCK_SIZE, y + BLOCK_SIZE);
            g.drawLine(x - BLOCK_SIZE, y + BLOCK_SIZE, x + BLOCK_SIZE, y - BLOCK_SIZE);
        }
        return shape;
    }
}