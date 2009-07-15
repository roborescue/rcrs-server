package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Node;

/**
   A class that knows how to render nodes.
 */
public class NodeRenderer extends AbstractEntityRenderer {
    private static final int SIZE = 5;

    /**
       Construct a node renderer.
     */
    public NodeRenderer() {
        super(Node.class);
    }

    @Override
    public Shape render(StandardEntity e, Graphics2D g, ScreenTransform t, StandardWorldModel world) {
        Node n = (Node)e;
        int x = t.scaleX(n.getX()) - (SIZE / 2);
        int y = t.scaleY(n.getY()) - (SIZE / 2);
        g.setColor(Color.BLACK);
        Shape shape = new Ellipse2D.Double(x, y, SIZE, SIZE);
        g.fill(shape);
        return shape;
    }
}