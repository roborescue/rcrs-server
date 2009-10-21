package rescuecore2.standard.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.EntityIDComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.components.RawDataComponent;

import java.io.InputStream;
import java.io.IOException;

/**
   An kernel hear channel command.
 */
public class KAHearChannel extends AbstractMessage {
    private EntityIDComponent sender;
    private IntComponent channel;
    private RawDataComponent data;

    /**
       A KAHearChannel message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public KAHearChannel(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       Construct a hear-channel command.
       @param agent The ID of the agent that spoke.
       @param channel The ID of the channel that was used.
       @param data The content of the message.
    */
    public KAHearChannel(EntityID agent, int channel, byte[] data) {
        this();
        this.sender.setValue(agent);
        this.channel.setValue(channel);
        this.data.setData(data);
    }

    private KAHearChannel() {
        super(StandardMessageURN.KA_HEAR_CHANNEL);
        sender = new EntityIDComponent("Sender");
        channel = new IntComponent("Channel");
        data = new RawDataComponent("Message");
        addMessageComponent(sender);
        addMessageComponent(channel);
        addMessageComponent(data);
    }

    /**
       Get the sender of the message.
       @return The sender ID.
     */
    public EntityID getSenderID() {
        return sender.getValue();
    }

    /**
       Get the channel number.
       @return The channel number.
     */
    public int getChannel() {
        return channel.getValue();
    }

    /**
       Get the content of the message.
       @return The message content.
     */
    public byte[] getContent() {
        return data.getData();
    }
}