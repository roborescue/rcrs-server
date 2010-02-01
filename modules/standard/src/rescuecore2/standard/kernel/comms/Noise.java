package rescuecore2.standard.kernel.comms;

import rescuecore2.standard.messages.AKSpeak;

/**
   Noise implementations mess with messages in some way.
*/
public interface Noise {
    /**
       Optionally apply some noise to a message and return either the original message or a replacement.
       @param message The message to tinker with.
       @return The original message or a replacement.
    */
    AKSpeak applyNoise(AKSpeak message);
}

