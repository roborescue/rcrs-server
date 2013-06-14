package collapse;


import java.util.HashMap;

import rescuecore2.messages.control.KSAfterShocksInfo;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * Collapse simulator's world model contains aftershocks' information that other
 * simulators don't need.
 *
 * @author Salim
 *
 */
public class CollapseWorldModel extends StandardWorldModel {
	private HashMap<Integer, Float> aftershocks;
	private HashMap<Building, CSBuilding> collapseBuildings;

	public CollapseWorldModel() {
		this.aftershocks = new HashMap<Integer, Float>();
		collapseBuildings = new HashMap<Building, CSBuilding>();
	}

	@Override
	public void merge(ChangeSet changes) {
		super.merge(changes);
	}

	/**
	 * Changes the list in the world model with the new input aftershock list
	 *
	 * @param msg
	 *            instance of KSAfterShocksInfo
	 */
	public void updateAftershocks(KSAfterShocksInfo msg) {
		aftershocks = msg.getAftershocks();
	}

	public boolean aftershockHappens(int time) {
		return aftershocks.get(time) != null;
	}

	public float aftershockIntensity(int time) {
		return aftershocks.get(time);
	}

	public HashMap<Building, CSBuilding> getCollapseBuildings() {
		if (collapseBuildings.size() == 0)
			createCollapseBuildings();
		return collapseBuildings;
	}

	/**
	 * Creates Collapse Simulator Buildings using the Standard Buildings
	 */
	private void createCollapseBuildings() {
		for (StandardEntity entity : this) {
			if(entity instanceof Building)
				collapseBuildings.put((Building) entity, new CSBuilding((Building) entity));
		}

	}

	/**
	 * Returns a specific CSBuilding by its EntityID
	 *
	 * @param id
	 *            is an EntityID
	 * @return the corresponding CSBuilding to id
	 */
	public CSBuilding getCSBuiding(EntityID id) {
		return getCollapseBuildings().get((Building) getEntity(id));
	}

	/**
	 * Returns a specific CSBuilding by its Building
	 *
	 * @param building
	 *            is an Building
	 * @return the corresponding CSBuilding to building
	 */
	public CSBuilding getCSBuiding(Building building) {
		return getCollapseBuildings().get(building);
	}
}
