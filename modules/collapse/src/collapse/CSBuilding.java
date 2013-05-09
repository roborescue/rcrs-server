package collapse;

import rescuecore2.standard.entities.Building;

/**
 * Collapse Simulator Building (CSBuilding) is a wrapper for the Standard
 * Building class that contains extra variables created, updated and used by the
 * Collapse Simulator only. This class is created in order to prevented
 * unnecessary changes to the Standard Building class.
 * 
 * @author Salim
 * 
 */
public class CSBuilding {
	/**
	 * The reference to the real building class
	 */
	private final Building real;

	/**
	 * Collapse Ratio shows the percent that the building has been collapsed so
	 * far.
	 */
	private float collapsedRatio = 0;
	/**
	 * This shows whether the building has fire damage in the last cycle or not
	 */
	private boolean hasFireDamage = false;

	public CSBuilding(Building building) {
		real = building;
	}

	/**
	 * Returns the building's collapse ratio
	 * 
	 * @return
	 */
	public float getCollapsedRatio() {
		return collapsedRatio;
	}

	/**
	 * Changes the collapse ratio of the building to the input ratio
	 * 
	 * @param collapsedRatio
	 *            is a float
	 */

	public void setCollapsedRatio(float collapsedRatio) {
		this.collapsedRatio = collapsedRatio;
	}

	/**
	 * Adds the input ratio to the building's collapse ratio
	 * 
	 * @param ratio
	 *            is a float that represents the increased value of the collapse
	 *            ratio
	 */
	public void increaseCollapseRatio(float ratio) {
		setCollapsedRatio(getCollapsedRatio() + ratio);

	}

	public Building getReal() {
		return real;
	}

	/**
	 * Returns the extent that is still possible to collapse.
	 * 
	 * @return a float representing the extent
	 */
	public double getRemainingToCollapse(double floorHeight) {
		return floorHeight * real.getFloors() * (1 - getCollapsedRatio());
	}

	public boolean hasFireDamage() {
		return false;
	}

	public void setHasFireDamage(boolean hasFireDamage) {
		this.hasFireDamage = hasFireDamage;
	}
	public double getTotalCollapse(double floorHeight){
		return floorHeight*real.getFloors();
	}
}
