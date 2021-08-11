package rescuecore2.standard.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.components.IntListComponent;
import rescuecore2.messages.AbstractCommand;

import java.util.List;

import java.io.InputStream;
import java.io.IOException;

/**
   An agent channel subscription command.
 */
public class AKSubscribe extends AbstractCommand {
    private IntListComponent channels;

    /**
       An AKSubscribe message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public AKSubscribe(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       Construct a subscribe command.
       @param agent The ID of the agent issuing the command.
       @param time The time the command was issued.
       @param channels The IDs of the channels to speak on.
     */
    public AKSubscribe(EntityID agent, int time, int... channels) {
        this();
        setAgentID(agent);
        setTime(time);
        this.channels.setValues(channels);
    }

    private AKSubscribe() {
        super(StandardMessageURN.AK_SUBSCRIBE);
        channels = new IntListComponent("Channels");
        addMessageComponent(channels);
    }

    /**
       Get the channels that have been requested.
       @return The requested channels.
    */
    public List<Integer> getChannels() {
        return channels.getValues();
    }
}
