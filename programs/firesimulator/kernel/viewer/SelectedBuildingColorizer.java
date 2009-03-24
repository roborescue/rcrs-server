package firesimulator.kernel.viewer;

import java.awt.Color;

import firesimulator.world.Building;
import firesimulator.world.RescueObject;

/**
 * @author tn
 *
 */
public class SelectedBuildingColorizer implements BuildingColorizer, SelectorListener {

	Building selected;

	public Color getColor(Building b) {
		if (selected==null) return Color.DARK_GRAY;
		if (selected==b){System.out.println(b.getBuildingAreaGround());return Color.RED;}		
		return Color.YELLOW;
	}


	public void select(RescueObject o,int modifier) {
		if(o==null)return;
		if(o instanceof Building)selected=(Building)o;
	}

	public String toString(){
		return "selected";
	}

}
