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

public class Buildings extends GUILayer {

	@Override
	public void paint(PaintEvent paintEvent) {
		for (Iterator i = paintEvent.getWorld().getBuildings().iterator(); i.hasNext();) {
			Building b = (Building) i.next();
			Polygon polygon = new Polygon();
			int apexes[] = b.getApexes();
			for (int n = 0; n < apexes.length; n++) {
				int x = apexes[n];
				int y = apexes[++n];
				polygon.addPoint(x, y);
			}
			polygon = paintEvent.getTransform().getTransformedPolygon(polygon);
			paintEvent.getGraphics2D().setColor(Color.gray);
			paintEvent.getGraphics2D().fillPolygon(polygon);
			paintEvent.getGraphics2D().setColor(Color.darkGray);
			paintEvent.getGraphics2D().drawPolygon(polygon);
		}
	}

}
