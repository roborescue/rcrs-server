package kernel;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.config.Config;

/**
   Implementations of this class are responsible for creating the initial world model.
   @param <T> The subclass of Entity that this world model creator understands.
 */
public interface WorldModelCreator<T extends Entity> {
    /**
       Create a new world model.
       @param config The config to use.
       @return A new world model.
       @throws KernelException If there is a problem building the world model.
    */
    WorldModel<T> buildWorldModel(Config config) throws KernelException;
}