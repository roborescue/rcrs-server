package rescuecore2.standard.score;

import rescuecore2.score.ScoreFunction;
import rescuecore2.score.DelegatingScoreFunction;
import rescuecore2.score.UnaryOperatorScoreFunction;
import rescuecore2.score.WeightedScoreFunction;
import rescuecore2.score.MultiplicativeScoreFunction;

/**
   Implementation of the legacy score function. Score = sqrt(building area saved) * (civilians alive + civilian health)
*/
public class LegacyScoreFunction extends DelegatingScoreFunction {
    /**
       Construct a LegacyScoreFunction.
    */
    public LegacyScoreFunction() {
        super(makeFunction());
    }

    @Override
    public String toString() {
        return "Legacy score function";
    }

    private static ScoreFunction makeFunction() {
        ScoreFunction buildings = new UnaryOperatorScoreFunction(UnaryOperatorScoreFunction.Operator.SQUARE_ROOT, new BuildingDamageScoreFunction());
        WeightedScoreFunction civs = new WeightedScoreFunction();
        civs.addChildFunction(new CiviliansAliveScoreFunction());
        civs.addChildFunction(new HealthScoreFunction());
        return new MultiplicativeScoreFunction(civs, buildings);
    }
}