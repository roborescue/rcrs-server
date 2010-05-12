package sample;

import java.util.Collection;
import java.util.EnumSet;

import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;

import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.messages.AKSpeak;

import rescuecore2.log.Logger;

/**
   An agent for testing communication channels.
 */
public class ChannelTestAgent extends AbstractSampleAgent<Human> {
    private static final int CHANNEL = 4;
    private static final int N = 100;

    @Override
    public String toString() {
        return "Channel test agent";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        Logger.info("Channel test agent " + getID() + " connected");
    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            sendSubscribe(time, CHANNEL);
        }
        // Send N messages
        if (me() instanceof FireBrigade) {
            for (int i = 0; i < N; ++i) {
                say(i + 1, time);
            }
        }
        Logger.debug("Time " + time);
        Logger.debug("Heard " + heard.size() + " messages");
        // Count failures and dropouts
        int failures = N;
        int dropout = 0;
        for (Command next : heard) {
            if (next instanceof AKSpeak) {
                AKSpeak speak = (AKSpeak)next;
                --failures;
                if (speak.getContent().length == 0) {
                    ++dropout;
                }
            }
        }
        Logger.debug(failures + " failed messages");
        Logger.debug(dropout + " dropout messages");
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM);
    }

    private void say(int messageLength, int time) {
        sendSpeak(time, CHANNEL, new byte[messageLength]);
    }
}
