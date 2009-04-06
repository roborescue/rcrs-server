package firesimulator.kernel.viewer;

import java.awt.Graphics2D;

import firesimulator.world.World;

/**
 * @author tn
 *
 */
public class NullPainter implements Painter {

	
	public void paint(Graphics2D g, World w, BuildingColorizer col) {}
	
	public String toString(){
		return "null";
	}

	public void paint(Graphics2D g, World w, AirColorizer col) {
	}

}
