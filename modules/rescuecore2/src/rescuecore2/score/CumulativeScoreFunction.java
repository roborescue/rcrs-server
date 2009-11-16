package rescuecore2.score;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

import java.util.Collection;

/**
   A score function that accumulates scores from a set of child score functions.
 */
public class CumulativeScoreFunction extends CompositeScoreFunction {
    private double sum;

    /**
       Create a CumulativeScoreFunction with no children.
    */
    public CumulativeScoreFunction() {
        super();
    }

    /**
       Create a CumulativeScoreFunction with a collection of children.
       @param children The child score functions.
    */
    public CumulativeScoreFunction(Collection<ScoreFunction> children) {
        super(children);
    }

    /**
       Create a CumulativeScoreFunction with a collection of children.
       @param children The child score functions.
    */
    public CumulativeScoreFunction(ScoreFunction... children) {
        super(children);
    }

    @Override
    public void initialise(Config config) {
        super.initialise(config);
        sum = 0;
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        for (ScoreFunction next : children) {
            sum += next.score(world, timestep);
        }
        return sum;
    }
}