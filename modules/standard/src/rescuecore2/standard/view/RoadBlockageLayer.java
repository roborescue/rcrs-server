package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Polygon;

import rescuecore2.standard.entities.Blockade;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A view layer that renders road blockages.
 */
public class RoadBlockageLayer extends StandardEntityViewLayer<Blockade> {
    private static final int BLOCK_SIZE = 3;
    private static final int BLOCK_STROKE_WIDTH = 2;

    private static final Color COLOUR = Color.black;

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
    	if(!b.isApexesDefined())
    		return null;
        int[] apexes = b.getApexes();
        int count = apexes.length / 2;
        int[] xs = new int[count];
        int[] ys = new int[count];
        for (int i = 0; i < count; ++i) {
            xs[i] = t.xToScreen(apexes[i * 2]);
            ys[i] = t.yToScreen(apexes[(i * 2) + 1]);
        }
        Polygon shape = new Polygon(xs, ys, count);
        g.setColor(COLOUR);
        g.fill(shape);
        return shape;
    }
}
