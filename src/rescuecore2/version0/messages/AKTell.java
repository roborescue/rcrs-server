package rescuecore2.version0.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.RawDataComponent;

/**
   An agent TELL command.
 */
public class AKTell extends AgentCommand {
    private IntComponent channel;
    private RawDataComponent data;

    /**
       Create an empty AKTell command.
     */
    AKTell() {
        super("AK_TELL", MessageConstants.AK_TELL);
        init();
    }

    /**
       Construct a tell command.
       @param agent The ID of the agent issuing the command.
       @param data The content of the command.
       @param time The time the command was issued.
     */
    public AKTell(EntityID agent, byte[] data, int time) {
        super("AK_TELL", MessageConstants.AK_TELL, agent, time);
        init();
        this.data.setData(data);
    }

    /**
       Get the content of the message.
       @return The message content.
     */
    public byte[] getContent() {
        return data.getData();
    }

    private void init() {
        channel = new IntComponent("Channel");
        data = new RawDataComponent("Message");
        addMessageComponent(channel);
        addMessageComponent(data);
    }
}