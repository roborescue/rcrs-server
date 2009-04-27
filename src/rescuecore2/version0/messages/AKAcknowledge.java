package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;

/**
   A message for acknowleding a connection to the kernel.
 */
public class AKAcknowledge extends AbstractMessage {
    private IntComponent id;

    public AKAcknowledge() {
        super("AK_ACKNOWLEDGE", MessageConstants.AK_ACKNOWLEDGE);
        id = new IntComponent("ID");
        addMessageComponent(id);        
    }

    public AKAcknowledge(int id) {
        super("AK_ACKNOWLEDGE", MessageConstants.AK_ACKNOWLEDGE);
        this.id = new IntComponent("ID", id);
        addMessageComponent(this.id);
    }

    public int getAgentID() {
        return id.getValue();
    }
}