package rescuecore2.score;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

/**
   A score function that returns a constant score.
 */
public class ConstantScoreFunction extends AbstractScoreFunction {
    private double score;

    /**
       Create a ConstantScoreFunction.
       @param name The name of this function.
       @param score The constant score.
    */
    public ConstantScoreFunction(String name, double score) {
        super(name);
        this.score = score;
    }

    @Override
    public String toString() {
        return "Constant score";
    }

    @Override
    public void initialise(WorldModel<? extends Entity> world, Config config) {
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        return score;
    }
}
