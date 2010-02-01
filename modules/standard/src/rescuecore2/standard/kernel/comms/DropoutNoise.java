package rescuecore2.standard.kernel.comms;

import java.util.Random;

import rescuecore2.standard.messages.AKSpeak;

/**
   Dropout noise completely zeroes a message with some probability.
*/
public class DropoutNoise implements Noise {
    private double p;
    private Random random;

    /**
       Construct a DropoutNoise object that will wipe out messages with some probability.
       @param p The probability of destroying a message.
       @param random The RNG to use.
    */
    public DropoutNoise(double p, Random random) {
        this.p = p;
        this.random = random;
    }

    @Override
    public AKSpeak applyNoise(AKSpeak message) {
        if (random.nextDouble() >= p) {
            return message;
        }
        return new AKSpeak(message.getAgentID(), message.getTime(), message.getChannel(), new byte[0]);
    }
}

