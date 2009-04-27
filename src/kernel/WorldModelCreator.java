package kernel;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.config.Config;

/**
   Implementations of this class are responsible for creating the initial world model.
 */
public interface WorldModelCreator {
    /**
       Build the world model.
       @param config The config to use.
       @throws KernelException If there is a problem building the world model.
    */
    WorldModel buildWorldModel(Config config) throws KernelException;
}