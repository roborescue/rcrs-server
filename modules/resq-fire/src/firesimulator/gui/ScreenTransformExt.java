package firesimulator.gui;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import rescuecore2.misc.gui.ScreenTransform;

/**
 *
 * Created by Alireza Kandeh on March 2018
 */

public class ScreenTransformExt extends ScreenTransform {

	public ScreenTransformExt(double minX, double minY, double maxX, double maxY) {
		super(minX, minY, maxX, maxY);
	}

	public Polygon getTransformedPolygon(Shape shape) {
		Polygon polygon = (Polygon) shape;
		Polygon result = new Polygon();
		for(int i = 0; i < polygon.npoints; i++) {
			result.addPoint(this.xToScreen(polygon.xpoints[i]), this.yToScreen(polygon.ypoints[i]));
		}
		return result;
	}

	public Rectangle2D getTransformedRectangle(Rectangle2D rect) {
		return new Rectangle(
			xToScreen(rect.getMinX()),
			yToScreen(rect.getMinY() + rect.getHeight()),
			xToScreen(rect.getWidth()) - xToScreen(0),
			yToScreen(0) - yToScreen(rect.getHeight())
		);
	}

	public Rectangle2D getTransformedRectangle(double x0, double y0, double w, double h) {
		return new Rectangle(
			xToScreen(x0),
			yToScreen(y0 + h),
			xToScreen(w) - xToScreen(0),
			yToScreen(0) - yToScreen(h)
		);
	}
	
	public void drawTransformedLine(Graphics2D g2, double x0, double y0, double x1, double y1) {
		g2.drawLine(
			this.xToScreen(x0),
			this.yToScreen(y0),
			this.xToScreen(x1),
			this.yToScreen(y1)
		);
	}
	
	public void fillTransformedOvalFixedRadius(Graphics2D g2, double x0, double y0, double r) {
		g2.fillOval(
			(int) (this.xToScreen(x0) - r),
			(int) (this.yToScreen(y0) - r),
			(int) (2 * r),
			(int) (2 * r)
		);
	}
    
}
