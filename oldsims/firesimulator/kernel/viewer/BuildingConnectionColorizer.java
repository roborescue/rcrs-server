package firesimulator.kernel.viewer;

import java.awt.Color;

import firesimulator.world.Building;
import firesimulator.world.RescueObject;

/**
 * @author Timo N�ssle
 *
 */
public class BuildingConnectionColorizer	implements BuildingColorizer, SelectorListener 
{
	Building selected;

	public Color getColor(Building b) {
		if (selected==null) return Color.DARK_GRAY;		
		if (selected==b)return Color.RED;
		int tmp=inConection(b);
		if (tmp!=-1){
			float w=selected.connectedValues[tmp];
			return new Color(0f,w,1f);
		}
		return Color.DARK_GRAY;
	}

	private int inConection(Building b){		
		if (selected==null) return -1;		
		for(int i=0;i<selected.connectedBuilding.length;i++){
			if (b==selected.connectedBuilding[i])
				return i;
		}
		return -1;
	}

	public void select(RescueObject o,int modifier) {
		if(o==null)return;
		if(o instanceof Building)selected=(Building)o;
	}

	public String toString(){
		return "connected buildings";
	}


}
