package rescuecore2.messages.control;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;

/**
   A message for acknowleding a connection to the kernel.
 */
public class AKAcknowledge extends AbstractMessage {
    private IntComponent requestID;

    /**
       AKAcknowledge message with an undefined request ID component.
     */
    public AKAcknowledge() {
        super("AK_ACKNOWLEDGE", ControlMessageConstants.AK_ACKNOWLEDGE);
        requestID = new IntComponent("Request ID");
        addMessageComponent(requestID);
    }

    /**
       AKAcknowledge message with a specific request ID component.
       @param id The value of the request ID component.
     */
    public AKAcknowledge(int id) {
        this();
        this.requestID.setValue(id);
    }

    /**
       Get the request ID of this acknowledgement.
       @return The request ID component.
     */
    public int getRequestID() {
        return requestID.getValue();
    }
}