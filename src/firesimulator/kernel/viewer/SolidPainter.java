package firesimulator.kernel.viewer;

import java.awt.Graphics2D;
import java.util.Iterator;

import firesimulator.world.Building;
import firesimulator.world.World;

/**
 * @author tn
 *

 */
public class SolidPainter implements Painter {

	public void paint(Graphics2D g, World world, BuildingColorizer col) {
		for(Iterator i=world.getBuildings().iterator();i.hasNext();){							
			Building b=(Building) i.next();
			g.setColor(col.getColor(b));
			g.fillPolygon(b.getPolygon());
		}		
	}
	
	public String toString(){
		return "solid";
	}

	public void paint(Graphics2D g, World w, AirColorizer col) {
		double[][]air=w.getAirTemp();				
		int oX=w.getMinX();
		int oY=w.getMinY();
		for(int x=0;x<air.length;x++)
			for(int y=0;y<air[x].length;y++){
				g.setColor(col.getColor(w,x,y));
				g.fillRect(oX+x*w.SAMPLE_SIZE,oY+y*w.SAMPLE_SIZE,w.SAMPLE_SIZE,w.SAMPLE_SIZE);
			}
	}

}
