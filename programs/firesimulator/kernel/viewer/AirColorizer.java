package firesimulator.kernel.viewer;

import java.awt.Color;

import firesimulator.world.World;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface AirColorizer {

	public Color getColor(World world,int x,int y);

}
