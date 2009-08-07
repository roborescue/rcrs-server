package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Polygon;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.Node;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A view layer that renders buildings.
 */
public class AreaLayer extends StandardEntityViewLayer<Area> {
    private static final int SLIGHTLY_BROKEN = 25;
    private static final int QUITE_BROKEN = 50;
    private static final int VERY_BROKEN = 75;

    /**
       Construct a building view layer.
     */
    public AreaLayer() {
        super(Area.class);
    }

    @Override
    public String getName() {
        return "Area";
    }

    @Override
    public Shape render(Area a, Graphics2D g, ScreenTransform t) {
        int[] apexes = a.getApexes();
        int count = apexes.length / 2;
        int[] xs = new int[count];
        int[] ys = new int[count];
        for (int i = 0; i < count; ++i) {
            xs[i] = t.xToScreen(apexes[i * 2]);
            ys[i] = t.yToScreen(apexes[(i * 2) + 1]);
        }
        Polygon shape = new Polygon(xs, ys, count);
        g.setColor(Color.GRAY);
        g.fill(shape);
	g.setColor(Color.BLACK);
	if(a.isBlockadeListDefined()) {
	    for(EntityID id : a.getBlockadeList()) {
		Blockade blockade = (Blockade)world.getEntity(id);
		apexes = blockade.getShape();
		count = apexes.length /2;
		xs = new int[count];
		ys = new int[count];
		for (int i = 0; i < count; ++i) {
		    xs[i] = t.xToScreen(apexes[i * 2]);
		    ys[i] = t.yToScreen(apexes[(i * 2) + 1]);
		}
		shape = new Polygon(xs, ys, count);
		g.fill(shape);
	    }
	    
	}

        //g.setColor(Color.BLACK);
        //g.draw(shape);
        // Draw a line to each entrance
        //int x = t.scaleX(a.getX());
        //int y = t.scaleY(a.getY());
        g.setColor(Color.LIGHT_GRAY);

        return shape;
    }
}