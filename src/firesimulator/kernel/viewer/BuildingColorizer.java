package firesimulator.kernel.viewer;

import java.awt.Color;

import firesimulator.world.Building;

/**
 * @author tn
 *
 */
public interface BuildingColorizer {

	public Color getColor(Building b);
	
}
