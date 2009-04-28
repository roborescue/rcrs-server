package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;

/**
   A message for acknowleding a connection to the kernel.
 */
public class AKAcknowledge extends AbstractMessage {
    private IntComponent id;

    /**
       AKAcknowledge message with an undefined ID component.
     */
    public AKAcknowledge() {
        super("AK_ACKNOWLEDGE", MessageConstants.AK_ACKNOWLEDGE);
        id = new IntComponent("ID");
        addMessageComponent(id);
    }

    /**
       AKAcknowledge message with a specific ID component.
       @param id The value of the ID component.
     */
    public AKAcknowledge(int id) {
        super("AK_ACKNOWLEDGE", MessageConstants.AK_ACKNOWLEDGE);
        this.id = new IntComponent("ID", id);
        addMessageComponent(this.id);
    }

    /**
       Get the ID of the agent that is acknowledging the connection.
       @return The agent ID component.
     */
    public int getAgentID() {
        return id.getValue();
    }
}