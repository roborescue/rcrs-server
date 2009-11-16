package rescuecore2.score;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

import java.util.Collection;

/**
   A score function that returns a set child score functions multiplied together.
 */
public class MultiplicativeScoreFunction extends CompositeScoreFunction {
    /**
       Create a MultiplicativeScoreFunction with no children.
    */
    public MultiplicativeScoreFunction() {
        super();
    }

    /**
       Create a MultiplicativeScoreFunction with a collection of children.
       @param children The child score functions.
    */
    public MultiplicativeScoreFunction(Collection<ScoreFunction> children) {
        super(children);
    }

    /**
       Create a MultiplicativeScoreFunction with a collection of children.
       @param children The child score functions.
    */
    public MultiplicativeScoreFunction(ScoreFunction... children) {
        super(children);
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        double result = 1;
        for (ScoreFunction next : children) {
            result *= next.score(world, timestep);
        }
        return result;
    }
}