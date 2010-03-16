package rescuecore2.score;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

/**
   A score function that applies some function to the score of a child score function.
 */
public abstract class DelegatingScoreFunction extends AbstractScoreFunction {
    /** The child score function. */
    protected ScoreFunction child;

    /**
       Create a DelegatingScoreFunction with a child function.
       @param name The name of this function.
       @param c The child score function.
    */
    public DelegatingScoreFunction(String name, ScoreFunction c) {
        super(name);
        child = c;
    }

    @Override
    public void initialise(WorldModel<? extends Entity> world, Config config) {
        child.initialise(world, config);
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        return child.score(world, timestep);
    }

    /**
       Get the child function.
       @return The child function.
    */
    public ScoreFunction getChildFunction() {
        return child;
    }
}
