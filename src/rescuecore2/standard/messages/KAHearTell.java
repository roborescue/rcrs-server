package rescuecore2.standard.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.EntityIDComponent;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.RawDataComponent;

/**
   An kernel hear-tell command.
 */
public class KAHearTell extends AbstractMessage {
    private EntityIDComponent recipient;
    private EntityIDComponent sender;
    private IntComponent channel;
    private RawDataComponent data;

    /**
       Create an empty KAHearTell command.
     */
    KAHearTell() {
        super("KA_HEAR_TELL", MessageConstants.KA_HEAR_TELL);
        init();
    }

    /**
       Construct a hear-tell command.
       @param agent The ID of the agent that spoke.
       @param data The content of the message.
    */
    public KAHearTell(EntityID agent, byte[] data) {
        super("KA_HEAR_TELL", MessageConstants.KA_HEAR_TELL);
        init();
        this.sender.setValue(agent);
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
       Get the content of the message.
       @return The message content.
     */
    public byte[] getContent() {
        return data.getData();
    }

    private void init() {
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
}