package rescuecore2.score;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

/**
   A score function that returns the inverse of a child score function.
 */
public class InverseScoreFunction implements ScoreFunction {
    private ScoreFunction child;

    /**
       Create an InverseScoreFunction.
       @param child The child function to invert.
    */
    public InverseScoreFunction(ScoreFunction child) {
        this.child = child;
    }

    @Override
    public void initialise(Config config) {
        child.initialise(config);
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        return 1.0 / child.score(world, timestep);
    }
}