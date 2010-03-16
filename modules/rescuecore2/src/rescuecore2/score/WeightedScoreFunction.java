package rescuecore2.score;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

import java.util.Map;
import java.util.HashMap;

/**
   A score function that returns a weighted sum of child score functions.
 */
public class WeightedScoreFunction extends CompositeScoreFunction {
    private Map<ScoreFunction, Double> weights;

    /**
       Create a WeightedScoreFunction with no children.
       @param name The name of this function.
    */
    public WeightedScoreFunction(String name) {
        super(name);
        weights = new HashMap<ScoreFunction, Double>();
    }

    @Override
    public String toString() {
        return "Weighted sum";
    }

    /**
       Add a child score function with a weight.
       @param child The child score function to add.
       @param weight The weight of this child function.
     */
    public void addChildFunction(ScoreFunction child, double weight) {
        addChildFunction(child);
        weights.put(child, weight);
    }

    @Override
    public void removeChildFunction(ScoreFunction child) {
        super.removeChildFunction(child);
        weights.remove(child);
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        double sum = 0;
        for (ScoreFunction next : children) {
            double weight = weights.containsKey(next) ? weights.get(next) : 1;
            sum += weight * next.score(world, timestep);
        }
        return sum;
    }
}
