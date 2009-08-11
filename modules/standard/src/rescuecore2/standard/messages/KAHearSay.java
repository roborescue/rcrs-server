package rescuecore2.standard.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.EntityIDComponent;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.RawDataComponent;

import java.io.InputStream;
import java.io.IOException;

/**
   An kernel hear-say command.
 */
public class KAHearSay extends AbstractMessage {
    private EntityIDComponent recipient;
    private EntityIDComponent sender;
    private IntComponent channel;
    private RawDataComponent data;

    /**
       A KAHearSay message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public KAHearSay(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       Construct a hear-say command.
       @param agent The ID of the agent that spoke.
       @param data The content of the message.
    */
    public KAHearSay(EntityID agent, byte[] data) {
        this();
        this.sender.setValue(agent);
        this.data.setData(data);
    }

    private KAHearSay() {
        super("KA_HEAR_SAY", MessageConstants.KA_HEAR_SAY);
        sender = new EntityIDComponent("Sender");
        recipient = new EntityIDComponent("Recipient");
        data = new RawDataComponent("Message");
        channel = new IntComponent("Channel");
        recipient.setValue(new EntityID(0));
        addMessageComponent(recipient);
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
       Get the content of the message.
       @return The message content.
     */
    public byte[] getContent() {
        return data.getData();
    }
}