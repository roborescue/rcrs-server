package firesimulator.gui.layers;

import firesimulator.gui.*;
import firesimulator.world.Building;
import java.awt.BasicStroke;
import java.awt.Color;

/**
 *
 * Created by Alireza Kandeh on March 2018
 */

public class ConnectedBuildings extends GUILayer {

	@Override
	public void paint(PaintEvent paintEvent) {
		Building selectedBuilding = paintEvent.getSelectedBuilding();
		if(selectedBuilding == null) {
			return;
		}
		paintEvent.getGraphics2D().setColor(Color.yellow);
		paintEvent.getGraphics2D().setStroke(new BasicStroke(3));
		Building[] bs = selectedBuilding.connectedBuilding;
		float[] vs = selectedBuilding.connectedValues;
		for (int c = 0; c < vs.length; c++) {
			paintEvent.getTransform().drawTransformedLine(
					paintEvent.getGraphics2D(),
					selectedBuilding.getX(),
					selectedBuilding.getY(),
					bs[c].getX(),
					bs[c].getY()
			);
		}
	}

}
