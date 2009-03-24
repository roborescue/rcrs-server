package firesimulator.kernel.viewer;

import java.awt.Color;

import firesimulator.world.Building;

/**
 * @author tn
 *
 */
public class FierynessColorizer implements BuildingColorizer {

    
	public Color getColor(Building b) {
       return ViewerColors.getBuildingColorForViewer(b.getFieryness());
    }

	public String toString(){
		return "fieryness";
	}
	
}
