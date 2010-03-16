package rescuecore2.misc;

import org.uncommons.maths.number.NumberGenerator;

/**
   A NumberGenerator that rounds output from a downstream NumberGenerator.
*/
public class RoundingNumberGenerator implements NumberGenerator<Integer> {
    private NumberGenerator<? extends Number> downstream;

    /**
       Construct a RoundingNumberGenerator.
       @param downstream The downstream generator to round output from.
    */
    public RoundingNumberGenerator(NumberGenerator<? extends Number> downstream) {
        this.downstream = downstream;
    }

    @Override
    public Integer nextValue() {
        Number n = downstream.nextValue();
        return (int)Math.round(n.doubleValue());
    }
}
