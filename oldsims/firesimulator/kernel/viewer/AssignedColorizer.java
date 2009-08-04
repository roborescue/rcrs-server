package firesimulator.kernel.viewer;

import java.awt.Color;

import firesimulator.world.Building;
import firesimulator.world.RescueObject;
import firesimulator.world.World;

/**
 * @author tn
 *
 */
public class AssignedColorizer implements AirColorizer,SelectorListener {

	private World world;
	private Building selected;
	
	public AssignedColorizer(World w){
		world=w;
	}

	public Color getColor(World world, int x, int y) {
		if(selected==null) return Color.BLACK;
		for(int c=0;c<selected.cells.length;c++){
			if(selected.cells[c][0]==x&&selected.cells[c][1]==y)
				return new Color(0f,((float)selected.cells[c][2]/100f),0.4f);
		}
		return Color.BLACK;
	}

	public void select(RescueObject o,int modifier) {
		if(o==null)return;
		if(o instanceof Building)selected=(Building)o;
		else selected=null;
	}
	
	public String toString(){
		return "assigned";
	}
}
