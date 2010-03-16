package rescuecore2.standard.score;

import rescuecore2.score.ScoreFunction;
import rescuecore2.score.UnaryOperatorScoreFunction;
import rescuecore2.score.WeightedScoreFunction;
import rescuecore2.score.MultiplicativeScoreFunction;

/**
   Implementation of the legacy score function. Score = sqrt(building area saved) * (civilians alive + civilian health)
*/
public class LegacyScoreFunction extends MultiplicativeScoreFunction {
    /**
       Construct a LegacyScoreFunction.
    */
    public LegacyScoreFunction() {
        super("Overall");
        ScoreFunction buildings = new UnaryOperatorScoreFunction("Sqrt(building damage)", UnaryOperatorScoreFunction.Operator.SQUARE_ROOT, new BuildingDamageScoreFunction());
        WeightedScoreFunction civs = new WeightedScoreFunction("Civilian component");
        civs.addChildFunction(new CiviliansAliveScoreFunction());
        civs.addChildFunction(new HealthScoreFunction());
        addChildFunctions(civs, buildings);
    }
}
