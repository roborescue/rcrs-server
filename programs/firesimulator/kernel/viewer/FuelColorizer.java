package firesimulator.kernel.viewer;

import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;

import firesimulator.world.Building;

/**
 * @author tn
 *
 */
public class FuelColorizer implements BuildingColorizer {

	float maxFuel=0;	

	public FuelColorizer(){}
	
	public void init(Collection buildings){
		maxFuel=Integer.MIN_VALUE;
		for(Iterator i=buildings.iterator();i.hasNext();){
			Building b=(Building)i.next();						
			if(b.getInitialFuel()>maxFuel)
				maxFuel=b.getInitialFuel();
		}		
	}
	

	public Color getColor(Building b) {
		if(maxFuel==0)return Color.ORANGE;
		float p=((float)b.fuel)/((float)maxFuel);
		if(p<=0.32)return new Color(p*3,0,0);
		if(p<=0.65)return new Color(1f,p*1.5f,0);
		return new Color(1f,1f,p);
		
	}
	
	public String toString(){
		return "fuel";
	}

}
