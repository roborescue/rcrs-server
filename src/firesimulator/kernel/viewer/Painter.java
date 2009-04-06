package firesimulator.kernel.viewer;

import java.awt.Graphics2D;

import firesimulator.world.World;

/**
 * @author tn
 *
 */
public interface Painter {

	public void paint(Graphics2D g,World w, BuildingColorizer col);
	
	public void paint(Graphics2D g,World w, AirColorizer col);

}
