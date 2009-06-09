package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;

/**
   A message for acknowleding a connection to the kernel.
 */
public class VKAcknowledge extends AbstractMessage implements Control {
    private IntComponent requestID;

    /**
       An VKAcknowledge message with an undefined request ID component.
     */
    public VKAcknowledge() {
        super("VK_ACKNOWLEDGE", ControlMessageConstants.VK_ACKNOWLEDGE);
        requestID = new IntComponent("Request ID");
        addMessageComponent(requestID);
    }

    /**
       A VKAcknowledge with a request ID.
       @param requestID The request ID.
    */
    public VKAcknowledge(int requestID) {
        this();
        this.requestID.setValue(requestID);
    }

    /**
       Get the request ID.
       @return The request ID.
     */
    public int getRequestID() {
        return requestID.getValue();
    }
}