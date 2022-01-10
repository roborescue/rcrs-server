package rescuecore2.standard.score;

import rescuecore2.Timestep;
import rescuecore2.config.Config;
import rescuecore2.score.CompositeScoreFunction;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

public class RSL21ScoreFunction extends CompositeScoreFunction {

  private static final double MAX = 10000;

  public RSL21ScoreFunction() {
    super("RSL21 Score");
  }


  @Override
  public void initialise(WorldModel<? extends Entity> world, Config config) {}


  @Override
  public double score(WorldModel<? extends Entity> world, Timestep timestep) {
    double civilians = 0;
    double hp = 0;
    double max = 0;
    for (Entity next : world) {
      if (next instanceof Civilian) {
        Civilian c = (Civilian) next;
        if (c.isHPDefined()) {
          hp += c.getHP();
          if (c.getHP() > 0) {
            ++civilians;
          }
        }
        max += MAX;
      }
    }

    return civilians * Math.exp(-5 * (1 - (hp / max)));
  }
}