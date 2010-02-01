package rescuecore2.standard.kernel.comms;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import rescuecore2.standard.messages.AKSpeak;

/**
   A Noise implementation that chains Noise objects together.
*/
public class ChainedNoise implements Noise {
    private List<Noise> chain;

    /**
       Create a ChainedNoise object with no children.
    */
    public ChainedNoise() {
        chain = new ArrayList<Noise>();
    }

    /**
       Create a ChainedNoise object with a set of children.
       @param chain The child noise objects.
    */
    public ChainedNoise(Collection<Noise> chain) {
        this();
        this.chain.addAll(chain);
    }

    @Override
    public AKSpeak applyNoise(AKSpeak message) {
        AKSpeak current = message;
        for (Noise next : chain) {
            if (current == null) {
                return null;
            }
            current = next.applyNoise(current);
        }
        return current;
    }

    /**
       Add a child.
       @param child The child to add. This may be null.
    */
    public void addChild(Noise child) {
        if (child != null) {
            chain.add(child);
        }
    }

    /**
       Remove a child.
       @param child The child to remove.
    */
    public void removeChild(Noise child) {
        chain.remove(child);
    }
}

