package rescuecore2.score;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

/**
   A score function that returns a constant score.
 */
public class ConstantScoreFunction implements ScoreFunction {
    private double score;

    /**
       Create a ConstantScoreFunction.
       @param score The constant score.
    */
    public ConstantScoreFunction(double score) {
        this.score = score;
    }

    @Override
    public void initialise(Config config) {
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        return score;
    }
}