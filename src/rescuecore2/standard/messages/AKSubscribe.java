package rescuecore2.standard.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.IntListComponent;
import rescuecore2.messages.AbstractCommand;

import java.util.List;

/**
   An agent channel subscription command.
 */
public class AKSubscribe extends AbstractCommand {
    private IntListComponent channels;

    /**
       Create an empty AKSubscribe command.
     */
    AKSubscribe() {
        super("AK_SUBSCRIBE", MessageConstants.AK_SUBSCRIBE);
        init();
    }

    /**
       Construct a subscribe command.
       @param agent The ID of the agent issuing the command.
       @param time The time the command was issued.
       @param channels The IDs of the channels to speak on.
     */
    public AKSubscribe(EntityID agent, int time, int... channels) {
        super("AK_SUBSCRIBE", MessageConstants.AK_SUBSCRIBE, agent, time);
        init();
        this.channels.setValues(channels);
    }

    /**
       Get the channels that have been requested.
       @return The requested channels.
    */
    public List<Integer> getChannels() {
        return channels.getValues();
    }

    private void init() {
        channels = new IntListComponent("Channels");
        addMessageComponent(channels);
    }
}