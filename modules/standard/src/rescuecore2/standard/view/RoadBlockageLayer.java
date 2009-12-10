package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.BasicStroke;

import rescuecore2.standard.entities.Blockade;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A view layer that renders road blockages.
 */
public class RoadBlockageLayer extends StandardEntityViewLayer<Blockade> {
    private static final int BLOCK_SIZE = 3;
    private static final int BLOCK_STROKE_WIDTH = 2;

    private static final Color PARTIAL_BLOCK_COLOUR = Color.gray.darker();
    private static final Color TOTAL_BLOCK_COLOUR = Color.black;

    /**
       Construct a road blockage rendering layer.
     */
    public RoadBlockageLayer() {
        super(Blockade.class);
    }

    @Override
    public String getName() {
        return "Road blockages";
    }

    @Override
    public Shape render(Blockade b, Graphics2D g, ScreenTransform t) {
        return null;
    }
}