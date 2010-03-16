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
       @param name The name of this function.
    */
    public MultiplicativeScoreFunction(String name) {
        super(name);
    }

    /**
       Create a MultiplicativeScoreFunction with a collection of children.
       @param name The name of this function.
       @param children The child score functions.
    */
    public MultiplicativeScoreFunction(String name, Collection<ScoreFunction> children) {
        super(name, children);
    }

    /**
       Create a MultiplicativeScoreFunction with a collection of children.
       @param name The name of this function.
       @param children The child score functions.
    */
    public MultiplicativeScoreFunction(String name, ScoreFunction... children) {
        super(name, children);
    }

    @Override
    public String toString() {
        return "Multiplier";
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
