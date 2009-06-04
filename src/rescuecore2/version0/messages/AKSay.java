package rescuecore2.version0.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.RawDataComponent;

/**
   An agent SAY command.
 */
public class AKSay extends AgentCommand {
    private RawDataComponent data;

    /**
       Create an empty AKSay command.
     */
    AKSay() {
        super("AK_SAY", MessageConstants.AK_SAY);
        init();
    }

    /**
       Construct a say command.
       @param agent The ID of the agent issuing the command.
       @param data The content of the command.
       @param time The time the command was issued.
     */
    public AKSay(EntityID agent, byte[] data, int time) {
        super("AK_SAY", MessageConstants.AK_SAY, agent, time);
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
        data = new RawDataComponent("Message");
        addMessageComponent(data);
    }
}