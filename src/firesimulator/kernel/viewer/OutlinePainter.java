package firesimulator.kernel.viewer;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.util.Iterator;

import firesimulator.world.Building;
import firesimulator.world.World;

/**
 * @author tn
 */
public class OutlinePainter implements Painter {


	public void paint(Graphics2D g, World world, BuildingColorizer col) {
		g.setStroke(new BasicStroke(300f));
		for(Iterator i=world.getBuildings().iterator();i.hasNext();){							
			Building b=(Building) i.next();
			g.setColor(col.getColor(b));
			g.drawPolygon(b.getPolygon());
		}		
	}
	
	public String toString(){
		return "outline";
	}

	public void paint(Graphics2D g, World w, AirColorizer col) {
		g.setStroke(new BasicStroke(300f));
		double[][]air=w.getAirTemp();
		int oX=w.getMinX();
		int oY=w.getMinY();
		for(int x=0;x<air.length;x++)
			for(int y=0;y<air[x].length;y++){
				g.setColor(col.getColor(w,x,y));
				g.drawRect(oX+x*w.SAMPLE_SIZE,oY+y*w.SAMPLE_SIZE,w.SAMPLE_SIZE,w.SAMPLE_SIZE);
			}		
	}

}
