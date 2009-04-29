package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;

/**
   A message for acknowleding a connection to the kernel.
 */
public class VKAcknowledge extends AbstractMessage {
    private IntComponent id;

    /**
       VKAcknowledge message with an undefined ID component.
     */
    public VKAcknowledge() {
        super("VK_ACKNOWLEDGE", MessageConstants.VK_ACKNOWLEDGE);
        id = new IntComponent("ID");
        addMessageComponent(id);
    }

    /**
       VKAcknowledge message with a specific ID component.
       @param id The value of the ID component.
     */
    public VKAcknowledge(int id) {
        this();
        this.id.setValue(id);
    }

    /**
       Get the ID of the viewer that is acknowledging the connection.
       @return The viewer ID component.
     */
    public int getViewerID() {
        return id.getValue();
    }
}