package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;

/**
   A message for acknowleding a connection to the kernel.
 */
public class VKAcknowledge extends AbstractMessage implements Control {
    private IntComponent requestID;
    private IntComponent viewerID;

    /**
       VKAcknowledge message with a undefined request ID and viewer ID components.
     */
    public VKAcknowledge() {
        super("VK_ACKNOWLEDGE", ControlMessageConstants.VK_ACKNOWLEDGE);
        requestID = new IntComponent("Request ID");
        viewerID = new IntComponent("Viewer ID");
        addMessageComponent(requestID);
        addMessageComponent(viewerID);
    }

    /**
       VKAcknowledge message with specific request ID and viewer ID components.
       @param requestID The value of the request ID component.
       @param viewerID The value of the viewer ID component.
     */
    public VKAcknowledge(int requestID, int viewerID) {
        this();
        this.requestID.setValue(requestID);
        this.viewerID.setValue(viewerID);
    }

    /**
       Get the request ID.
       @return The request ID component.
     */
    public int getRequestID() {
        return requestID.getValue();
    }

    /**
       Get the viewer ID.
       @return The viewer ID component.
     */
    public int getViewerID() {
        return viewerID.getValue();
    }
}