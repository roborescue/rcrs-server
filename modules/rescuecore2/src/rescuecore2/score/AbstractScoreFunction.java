package rescuecore2.score;

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

    /**
       Change the name of this score function.
       @param newName The new name.
    */
    public void setName(String newName) {
        this.name = newName;
    }
}