package rescuecore2.standard.kernel.comms;

import java.util.Random;

import rescuecore2.standard.messages.AKSpeak;

/**
   Static noise flips bits in the message with some probability.
*/
public class StaticNoise implements Noise {
    private static final int BITS = 8;

    private double p;
    private Random random;

    /**
       Construct a StaticNoise object that will flip bits with some probability.
       @param p The probability of flipping a bit.
       @param random The RNG to use.
    */
    public StaticNoise(double p, Random random) {
        this.p = p;
        this.random = random;
    }

    @Override
    public AKSpeak applyNoise(AKSpeak message) {
        byte[] data = message.getContent();
        for (int i = 0; i < data.length; ++i) {
            for (int j = 0; j < BITS; ++j) {
                if (random.nextDouble() < p) {
                    // Flip this bit
                    data[i] = (byte)(data[i] ^ (1 << j));
                }
            }
        }
        return new AKSpeak(message.getAgentID(), message.getTime(), message.getChannel(), data);
    }
}
