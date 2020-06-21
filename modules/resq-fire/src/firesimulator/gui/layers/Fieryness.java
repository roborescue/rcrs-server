package firesimulator.gui.layers;

import firesimulator.gui.*;
import firesimulator.world.Building;
import java.awt.Color;
import java.awt.Polygon;
import java.util.Iterator;

/**
 *
 * Created by Alireza Kandeh on March 2018
 */

public class Fieryness extends GUILayer {
	
	private final static Color COLORS[] = new Color[] {
		new Color(176, 176,  56, 128),	// HEATING
		new Color(204, 122,  50, 128),	// BURNING
		new Color(160,  52,  52, 128),	// INFERNO
		new Color(50, 120, 130, 128),	// WATER_DAMAGE
		new Color(100, 140, 210, 128),	// MINOR_DAMAGE
		new Color(100, 70, 190, 128),	// MODERATE_DAMAGE
		new Color(80, 60, 140, 128),	// SEVERE_DAMAGE
		new Color(0, 0, 0, 255)			// BURNT_OUT
	};
	
	@Override
	public void paint(PaintEvent paintEvent) {
		for (Iterator i = paintEvent.getWorld().getBuildings().iterator(); i.hasNext();) {
			Building b = (Building) i.next();
			int f = b.getFieryness();
			if(f > 0) {
				Polygon polygon = new Polygon();
				int apexes[] = b.getApexes();
				for (int n = 0; n < apexes.length; n++) {
					int x = apexes[n];
					int y = apexes[++n];
					polygon.addPoint(x, y);
				}
				polygon = paintEvent.getTransform().getTransformedPolygon(polygon);
				paintEvent.getGraphics2D().setColor(COLORS[f -1]);
				paintEvent.getGraphics2D().fillPolygon(polygon);
			}
		}
	}

}
