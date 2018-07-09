package firesimulator.gui.layers;

import firesimulator.gui.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;

/**
 *
 * Created by Alireza Kandeh on March 2018
 */

public class AirCells extends GUILayer {

	@Override
	public void paint(PaintEvent paintEvent) {
		double cells[][] = paintEvent.getWorld().getAirTemp();
		int a = paintEvent.getWorld().SAMPLE_SIZE;
		int mx = paintEvent.getWorld().getMinX();
		int my = paintEvent.getWorld().getMinY();
		Color lightYellow = new Color(255, 255, 0, 15);
		paintEvent.getGraphics2D().setStroke(new BasicStroke(1));
		paintEvent.getGraphics2D().setColor(lightYellow);
		for(int x = 0; x <= cells.length; x++) {
			paintEvent.getTransform().drawTransformedLine(paintEvent.getGraphics2D(), x * a + mx, my, x * a + mx, (cells[0].length) * a + my);
		}
		for(int y = 0; y <= cells[0].length; y++) {
			paintEvent.getTransform().drawTransformedLine(paintEvent.getGraphics2D(), mx, y * a + my, (cells.length) * a + mx, y * a + my);
		}
		for(int x = 0; x < cells.length; x++) {
			for(int y = 0; y < cells[0].length; y++) {
				if(cells[x][y] > 0) {
					paintEvent.getGraphics2D().setColor(new Color(255, 0, 0, Math.min(255, (int) (cells[x][y] / 1500 * 255))));
					Rectangle2D rect = paintEvent.getTransform().getTransformedRectangle(x * a + mx, y * a + my, a, a);
					paintEvent.getGraphics2D().fill(rect);
				}
			}
		}
		int x = (paintEvent.getMouseX() - mx) / a;
		int y = (paintEvent.getMouseY() - my) / a;
		if(x >= 0 && x < cells.length && y >= 0 && y < cells[0].length) {
			Rectangle2D rect = paintEvent.getTransform().getTransformedRectangle(x * a + mx, y * a + my, a, a);
			paintEvent.getGraphics2D().setColor(Color.YELLOW);
			paintEvent.getGraphics2D().draw(rect);
		}
	}

	@Override
    public String getString(PaintEvent paintEvent) {
		int a = paintEvent.getWorld().SAMPLE_SIZE;
		int mx = paintEvent.getWorld().getMinX();
		int my = paintEvent.getWorld().getMinY();
		double cells[][] = paintEvent.getWorld().getAirTemp();
		int x = (paintEvent.getMouseX() - mx) / a;
		int y = (paintEvent.getMouseY() - my) / a;
		if(x >= 0 && x < cells.length && y >= 0 && y < cells[0].length) {
			return "Selected Air Cell Temperature: " + cells[x][y];
		}
		return null;
    }
	
}
