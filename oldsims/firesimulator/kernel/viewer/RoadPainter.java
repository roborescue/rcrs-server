package firesimulator.kernel.viewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Iterator;

import firesimulator.world.Road;
import firesimulator.world.World;

/**
 * @author tn
 *
 */
public class RoadPainter implements Painter {


	public void paint(Graphics2D g, World w, BuildingColorizer col) {
            /*
		g.setStroke(new BasicStroke(300));
		g.setColor(Color.GRAY);
		for(Iterator i=w.getRoads().iterator();i.hasNext();){
			Road r=(Road)i.next();
			g.drawLine(r.getHead().getX(),r.getHead().getY(),r.getTail().getX(),r.getTail().getY());
		}			
            */
	}
	
	public String toString(){
		return "simple";
	}


	public void paint(Graphics2D g, World w, AirColorizer col) {
	}

}
