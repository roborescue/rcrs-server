package rescuecore2.standard.kernel.comms;

import java.util.Random;

import rescuecore2.standard.messages.AKSpeak;

/**
   Failure noise drops an entire message with some probability.
*/
public class FailureNoise implements Noise {
    private double p;
    private Random random;

    /**
       Construct a FailureNoise object.
       @param p The probability of dropping a message.
       @param random The RNG to use.
    */
    public FailureNoise(double p, Random random) {
        this.p = p;
        this.random = random;
    }

    @Override
    public AKSpeak applyNoise(AKSpeak message) {
        if (random.nextDouble() < p) {
            return null;
        }
        return message;
    }
}
