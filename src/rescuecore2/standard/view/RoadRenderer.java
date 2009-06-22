package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.geom.Line2D;

import rescuecore2.worldmodel.Entity;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Node;
import rescuecore2.standard.entities.StandardWorldModel;

public class RoadRenderer extends AbstractEntityRenderer {
    private static final int LANE_WIDTH = 2;

    private StandardWorldModel model;

    public RoadRenderer(StandardWorldModel model) {
        super(Road.class);
        this.model = model;
    }

    @Override
    public Shape render(Entity e, Graphics2D g, ScreenTransform t) {
        Road r = (Road)e;
        Node head = (Node)r.getHead(model);
        Node tail = (Node)r.getTail(model);
        int headX = t.scaleX(head.getX());
        int headY = t.scaleY(head.getY());
        int tailX = t.scaleX(tail.getX());
        int tailY = t.scaleY(tail.getY());
        int lanes = r.getLinesToHead() + r.getLinesToTail();
        int block = r.getBlock();
        int width = r.getWidth();
        int laneWidth = width / lanes;
        int lanesBlocked = ((int)Math.floor(((block / 2) / laneWidth) + 0.5)) * 2;
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