package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Rectangle;

import rescuecore2.standard.entities.Node;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A view layer that renders nodes.
 */
public class NodeLayer extends StandardEntityViewLayer<Node> {
    private static final int SIZE = 2;
    private static final Color COLOUR = Color.gray.darker();

    /**
       Construct a node renderer.
     */
    public NodeLayer() {
        super(Node.class);
    }

    @Override
    public String getName() {
        return "Nodes";
    }

    @Override
    public Shape render(Node n, Graphics2D g, ScreenTransform t) {
        int x = t.xToScreen(n.getX());
        int y = t.yToScreen(n.getY());
        g.setColor(COLOUR);
        Shape shape = new Rectangle(x - SIZE / 2, y - SIZE / 2, SIZE, SIZE);
        g.fill(shape);
        return shape;
    }
}