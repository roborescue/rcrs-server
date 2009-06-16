package rescuecore2.standard.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.EntityIDComponent;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.RawDataComponent;

/**
   An kernel hear channel command.
 */
public class KAHearChannel extends AbstractMessage {
    private EntityIDComponent sender;
    private IntComponent channel;
    private RawDataComponent data;

    /**
       Create an empty KAHearChannel command.
     */
    KAHearChannel() {
        super("KA_HEAR_CHANNEL", MessageConstants.KA_HEAR_CHANNEL);
        init();
    }

    /**
       Construct a hear-channel command.
       @param agent The ID of the agent that spoke.
       @param channel The ID of the channel that was used.
       @param data The content of the message.
    */
    public KAHearChannel(EntityID agent, int channel, byte[] data) {
        super("KA_HEAR_CHANNEL", MessageConstants.KA_HEAR_CHANNEL);
        init();
        this.sender.setValue(agent);
        this.channel.setValue(channel);
        this.data.setData(data);
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

    private void init() {
        sender = new EntityIDComponent("Sender");
        channel = new IntComponent("Channel");
        data = new RawDataComponent("Message");
        addMessageComponent(sender);
        addMessageComponent(channel);
        addMessageComponent(data);
    }
}