package rescuecore2.version0.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.EntityIDComponent;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.RawDataComponent;

/**
   An kernel hear-say command.
 */
public class KAHearSay extends AbstractMessage {
    private EntityIDComponent recipient;
    private EntityIDComponent sender;
    private IntComponent channel;
    private RawDataComponent data;

    /**
       Create an empty KAHearSay command.
     */
    KAHearSay() {
        super("KA_HEAR_SAY", MessageConstants.KA_HEAR_SAY);
        init();
    }

    /**
       Construct a hear-say command.
       @param agent The ID of the agent that spoke.
       @param data The content of the message.
    */
    public KAHearSay(EntityID agent, byte[] data) {
        super("KA_HEAR_SAY", MessageConstants.KA_HEAR_SAY);
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