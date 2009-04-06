package firesimulator.kernel.viewer;

import java.awt.Color;

import firesimulator.world.Building;

/**
 * @author tn
 *
 */
public class BuildingCodeColorizer implements BuildingColorizer {

	final static Color WOOD=Color.GREEN;
	final static Color STEEL=Color.CYAN;
	final static Color CONCRET=Color.GRAY;
	final static Color UNKNOWN=Color.YELLOW;

	public Color getColor(Building b) {
		switch(b.getCode()){
			case 0:
				return WOOD;
			case 1:
				return STEEL;
			case 2:
				return CONCRET;
			default:
				return UNKNOWN;
		}		
	}
	
	public String toString(){
		return "code";
	}

}
