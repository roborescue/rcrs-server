package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.IntComponent;

import java.io.InputStream;
import java.io.IOException;

/**
   A message for acknowleding a connection to the kernel.
 */
public class VKAcknowledge extends AbstractMessage implements Control {
    private IntComponent requestID;
    private IntComponent viewerID;

    /**
       A VKAcknowledge message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public VKAcknowledge(InputStream in) throws IOException {
        this();
        read(in);
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

    private VKAcknowledge() {
        super(ControlMessageURN.VK_ACKNOWLEDGE);
        requestID = new IntComponent("Request ID");
        viewerID = new IntComponent("Viewer ID");
        addMessageComponent(requestID);
        addMessageComponent(viewerID);
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
