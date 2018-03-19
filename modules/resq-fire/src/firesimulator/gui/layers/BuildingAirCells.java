package firesimulator.gui.layers;

import firesimulator.gui.*;
import firesimulator.world.Building;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;

/**
 *
 * Created by Alireza Kandeh on March 2018
 */

public class BuildingAirCells extends GUILayer {

	@Override
	public void paint(PaintEvent paintEvent) {
		Building selectedBuilding = paintEvent.getSelectedBuilding();
		if(selectedBuilding == null) {
			return;
		}
		paintEvent.getGraphics2D().setStroke(new BasicStroke(1));
		int a = paintEvent.getWorld().SAMPLE_SIZE;
		int mx = paintEvent.getWorld().getMinX();
		int my = paintEvent.getWorld().getMinY();
		for(int[] cell : selectedBuilding.cells) {
			paintEvent.getGraphics2D().setColor(new Color(0, 255, 255, (int) ((float) cell[2] / 100 * 200)));
			Rectangle2D rect = paintEvent.getTransform().getTransformedRectangle(cell[0] * a + mx, cell[1] * a + my, a, a);
			paintEvent.getGraphics2D().fill(rect);
			paintEvent.getGraphics2D().setColor(Color.CYAN);
			paintEvent.getGraphics2D().draw(rect);
		}
	}

}
