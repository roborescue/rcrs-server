package firesimulator.gui;

import firesimulator.simulator.Simulator;
import firesimulator.world.Building;
import firesimulator.world.World;
import java.awt.Graphics2D;

/**
 *
 * Created by Alireza Kandeh on March 2018
 */

public class PaintEvent {

	private Graphics2D graphics2D = null;
	private ScreenTransformExt transform = null;
	private Simulator simulator = null;
	private World world = null;
	private Building selectedBuilding = null;
	private int mouseX = 0;
	private int mouseY = 0;

	public PaintEvent(Graphics2D graphics2D, ScreenTransformExt transform, Simulator simulator, World world, Building selectedBuilding, int mouseX, int mouseY) {
		this.graphics2D = graphics2D;
		this.transform = transform;
		this.simulator = simulator;
		this.world = world;
		this.selectedBuilding = selectedBuilding;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public Graphics2D getGraphics2D() {
		return graphics2D;
	}

	public ScreenTransformExt getTransform() {
		return transform;
	}
	
	public Simulator getSimulator() {
		return simulator;
	}
	
	public World getWorld() {
		return world;
	}
	
	public Building getSelectedBuilding() {
		return selectedBuilding;
	}

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}
	
}
