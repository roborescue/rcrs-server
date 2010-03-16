package rescuecore2.standard.score;

import rescuecore2.score.AbstractScoreFunction;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

import rescuecore2.standard.entities.Civilian;

/**
   Score function that measures the health of living civilians.
 */
public class HealthScoreFunction extends AbstractScoreFunction {
    private static final String ABSOLUTE_KEY = "score.standard.health.absolute";
    private static final double MAX = 10000;

    private boolean absolute;

    /**
       Construct a HealthScoreFunction.
    */
    public HealthScoreFunction() {
        super("Civilian health");
    }

    @Override
    public void initialise(WorldModel<? extends Entity> world, Config config) {
        absolute = config.getBooleanValue(ABSOLUTE_KEY, false);
        setName(absolute ? "Civilian health (absolute)" : "Civilian health (proportion)");
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        double sum = 0;
        double max = 0;
        for (Entity next : world) {
            if (next instanceof Civilian) {
                Civilian c = (Civilian)next;
                if (c.isHPDefined()) {
                    sum += c.getHP();
                }
                max += MAX;
            }
        }
        if (absolute) {
            return sum;
        }
        else {
            return sum / max;
        }
    }
}
