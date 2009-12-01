package kernel;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.config.Config;

/**
   The interface for world model creators, e.g. GIS systems.
 */
public interface WorldModelCreator extends EntityIDGenerator {
    /**
       Create a new WorldModel.
       @param config The config to use.
       @return A new world model.
       @throws KernelException If there is a problem building the world model.
    */
    WorldModel<? extends Entity> buildWorldModel(Config config) throws KernelException;
}