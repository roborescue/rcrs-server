package kernel;



import org.dom4j.DocumentException;

import rescuecore2.scenario.Scenario;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.config.Config;

/**
 * The interface for world model creators, e.g. GIS systems.
 */
public interface WorldModelCreator extends EntityIDGenerator {
	/**
	 * Create a new WorldModel.
	 * 
	 * @param config
	 *            The config to use.
	 * @return A new world model.
	 * @throws KernelException
	 *             If there is a problem building the world model.
	 */
	WorldModel<? extends Entity> buildWorldModel(Config config)
			throws KernelException;

	/**
	 * Returns the scenario of the simulation
	 * 
	 * @param config
	 * @return a scenario 
	 */
	public Scenario getScenario(Config config) throws DocumentException;
}
