package rescuecore2.standard.score;

import rescuecore2.score.ScoreFunction;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

import rescuecore2.standard.entities.Civilian;

/**
   Score function that measures the number of living civilians. One point per civilian still alive.
 */
public class CiviliansAliveScoreFunction implements ScoreFunction {
    @Override
    public String toString() {
        return "Civilians alive";
    }

    @Override
    public void initialise(WorldModel<? extends Entity> world, Config config) {
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        double sum = 0;
        for (Entity next : world) {
            if (next instanceof Civilian) {
                Civilian c = (Civilian)next;
                if (c.isHPDefined() && c.getHP() > 0) {
                    ++sum;
                }
            }
        }
        return sum;
    }
}