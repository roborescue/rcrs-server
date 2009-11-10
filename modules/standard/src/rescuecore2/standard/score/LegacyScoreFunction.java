package rescuecore2.standard.score;

import rescuecore2.Timestep;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.config.Config;
import rescuecore2.score.ScoreFunction;
import rescuecore2.score.UnaryOperatorScoreFunction;
import rescuecore2.score.WeightedScoreFunction;
import rescuecore2.score.MultiplicativeScoreFunction;

/**
   Implementation of the legacy score function. Score = sqrt(building area saved) * (civilians alive + civilian health)
*/
public class LegacyScoreFunction implements ScoreFunction {
    private ScoreFunction result;

    @Override
    public void initialise(Config config) {
        ScoreFunction buildings = new UnaryOperatorScoreFunction(UnaryOperatorScoreFunction.Operator.SQUARE_ROOT, new BuildingDamageScoreFunction());
        WeightedScoreFunction civs = new WeightedScoreFunction();
        civs.addChildFunction(new CiviliansAliveScoreFunction());
        civs.addChildFunction(new HealthScoreFunction());
        result = new MultiplicativeScoreFunction(civs, buildings);
        result.initialise(config);
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        return result.score(world, timestep);
    }

}