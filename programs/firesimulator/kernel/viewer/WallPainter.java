package firesimulator.kernel.viewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Iterator;

import firesimulator.world.Building;
import firesimulator.world.RescueObject;
import firesimulator.world.Wall;
import firesimulator.world.World;

/**
 * @author tn
 *
 */
public class WallPainter implements Painter, SelectorListener{

	int walln=1;
	Building selected;


	public void paint(Graphics2D g, World world, BuildingColorizer col) {
		g.setStroke(new BasicStroke(300f));
		g.setColor(Color.BLUE);
		for(Iterator i=world.getBuildings().iterator();i.hasNext();){										
			Building b=(Building) i.next();
			if(selected!=b){
				g.drawPolygon(b.getPolygon());
			}			
			else{
				int counter=0;
				for(Iterator walls=b.walls.iterator();walls.hasNext();counter++){
					Wall w=(Wall)walls.next();
					if(counter==walln)
						g.setColor(Color.GREEN);
					else
						g.setColor(Color.RED);
					g.drawLine(w.x1,w.y1,w.x2,w.y2);
				}
				g.setColor(Color.BLUE);			
			}									
		}		
	}
	
	public String toString(){
		return "wall";
	}

	public void paint(Graphics2D g, World w, AirColorizer col) {
	}

	public void select(RescueObject o, int modifier) {
		if(o instanceof Building&&modifier==1){
			selected=(Building)o;
			if(walln>=selected.walls.size())
				walln=0;
		}			
		if(modifier==3&&selected!=null){
			//System.out.println("in wall:"+walln+" of  "+selected.walls.size());
			walln++;
			if(walln==selected.walls.size())
				walln=0;
		}
	}

}
