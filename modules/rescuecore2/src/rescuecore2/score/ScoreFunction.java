package rescuecore2.score;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

/**
   Interface for a score function.
 */
public interface ScoreFunction {
    /**
       Initialise this score function.
       @param world The state of the world at the start of the simulation.
       @param config The system configuration.
    */
    void initialise(WorldModel<? extends Entity> world, Config config);

    /**
       Calculate the score for a timestep.
       @param world The state of the world at the end of the timestep.
       @param timestep The record of perception, commands and changes for the timestep.
       @return The score for this timestep.
     */
    double score(WorldModel<? extends Entity> world, Timestep timestep);

    /**
       Get the name of this score function.
       @return The name.
    */
    String getName();
}
