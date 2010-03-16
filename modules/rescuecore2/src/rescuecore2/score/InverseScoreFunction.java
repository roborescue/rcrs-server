package rescuecore2.score;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

/**
   A score function that returns the inverse of a child score function.
 */
public class InverseScoreFunction extends DelegatingScoreFunction {
    /**
       Create an InverseScoreFunction.
       @param name The name of this function.
       @param child The child function to invert.
    */
    public InverseScoreFunction(String name, ScoreFunction child) {
        super(name, child);
    }

    @Override
    public String toString() {
        return "Inverse";
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        return 1.0 / child.score(world, timestep);
    }
}
