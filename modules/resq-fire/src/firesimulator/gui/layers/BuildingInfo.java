package firesimulator.gui.layers;

import firesimulator.gui.*;
import firesimulator.world.Building;

/**
 *
 * Created by Alireza Kandeh on March 2018
 */

public class BuildingInfo extends GUILayer {

	@Override
	public void paint(PaintEvent paintEvent) {
	}

	@Override
	public String getString(PaintEvent paintEvent) {
		Building b = paintEvent.getSelectedBuilding();
		if(b == null) {
			return null;
		}
		String result = "";

		result += "ID:\t\t" + b.getID();
		result += "\n";
		
		result += "Temperature:\t\t" + b.getTemperature();
		result += "\n";
		
		result += "Energy:\t\t" + b.getEnergy();
		result += "\n";
		
		result += "Fuel:\t\t" + b.getFuel();
		result += "\n";
		
		result += "Water Quantity:\t" + b.getWaterQuantity();
		result += "\n";
		
		result += "Was Ever Watered:\t" + b.wasEverWatered();
		result += "\n";

		result += "Fieryness:\t\t" + b.getFieryness();
		result += "\n";

		result += "Floors:\t\t" + b.getFloors();
		result += "\n";

		result += "Code:\t\t" + b.getCode();
		result += "\n";
		
		
		result += "X:\t\t" + b.getX();
		result += "\n";
		
		result += "Y:\t\t" + b.getY();
		result += "\n";

		return result;
	}
}
