package rescuecore2.score;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
   A score function that accumulates scores from a set of child score functions.
 */
public class CumulativeScoreFunction extends CompositeScoreFunction {
    private Map<Integer, Double> scores;

    /**
       Create a CumulativeScoreFunction with no children.
       @param name The name of this function.
    */
    public CumulativeScoreFunction(String name) {
        super(name);
    }

    /**
       Create a CumulativeScoreFunction with a collection of children.
       @param name The name of this function.
       @param children The child score functions.
    */
    public CumulativeScoreFunction(String name, Collection<ScoreFunction> children) {
        super(name, children);
    }

    /**
       Create a CumulativeScoreFunction with a collection of children.
       @param name The name of this function.
       @param children The child score functions.
    */
    public CumulativeScoreFunction(String name, ScoreFunction... children) {
        super(name, children);
    }

    @Override
    public void initialise(WorldModel<? extends Entity> world, Config config) {
        super.initialise(world, config);
        scores = new HashMap<Integer, Double>();
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        double sum = 0;
        for (ScoreFunction next : children) {
            sum += next.score(world, timestep);
        }
        Double previous = scores.get(timestep.getTime() - 1);
        if (previous != null) {
            sum += previous;
        }
        scores.put(timestep.getTime(), sum);
        return sum;
    }
}
