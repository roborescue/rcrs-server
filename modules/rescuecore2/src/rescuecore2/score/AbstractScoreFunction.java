package rescuecore2.score;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

/**
   Abstract base class for a score function.
 */
public abstract class AbstractScoreFunction implements ScoreFunction {
    private String name;

    /**
       Construct an AbstractScoreFunction.
       @param name The name of this function.
    */
    protected AbstractScoreFunction(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void initialise(WorldModel<? extends Entity> world, Config config) {
    }

    /**
       Change the name of this score function.
       @param newName The new name.
    */
    public void setName(String newName) {
        this.name = newName;
    }
}
