package rescuecore2.score;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

/**
   A score function that performs a unary operation on a child score function.
 */
public class UnaryOperatorScoreFunction extends DelegatingScoreFunction {
    private Operator op;

    /**
       Create a UnaryOperatorScoreFunction.
       @param op The operation to perform.
       @param child The child function to invert.
    */
    public UnaryOperatorScoreFunction(Operator op, ScoreFunction child) {
        super(child);
        this.op = op;
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        return op.perform(child.score(world, timestep));
    }

    @Override
    public String toString() {
        return "Unary operator: " + op.toString();
    }

    /**
       Enum constants for the possible operations this class supports.
     */
    public static enum Operator {
        /** The inversion operator. Returns 1 / <child score>. */
        INVERSE {
            @Override
            protected double perform(double in) {
                return 1.0 / in;
            }
        },
        /** The square root operator. Returns Math.sqrt(<child score>). */
        SQUARE_ROOT {
            @Override
            protected double perform(double in) {
                return Math.sqrt(in);
            }
        };

        /**
           Perform the unary operation.
           @param in The input value.
           @return The output value.
        */
        protected abstract double perform(double in);
    }
}