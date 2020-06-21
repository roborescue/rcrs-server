package firesimulator.gui.layers;

import firesimulator.gui.*;
import firesimulator.world.Building;
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Iterator;

/**
 *
 * Created by Alireza Kandeh on March 2018
 */

public class AllConnections extends GUILayer {

	@Override
	public void paint(PaintEvent paintEvent) {
		paintEvent.getGraphics2D().setColor(Color.yellow);
		paintEvent.getGraphics2D().setStroke(new BasicStroke(1));
		for (Iterator i = paintEvent.getWorld().getBuildings().iterator(); i.hasNext();) {
			Building b = (Building) i.next();
            Building[] bs = b.connectedBuilding;
            float[] vs = b.connectedValues;
			for (int c = 0; c < vs.length; c++) {
				paintEvent.getTransform().drawTransformedLine(paintEvent.getGraphics2D(), b.getX(), b.getY(), bs[c].getX(), bs[c].getY());
			}
		}
	}

}
