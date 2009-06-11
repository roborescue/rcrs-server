package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;

/**
   A message for connecting a simulator to the kernel.
 */
public class SKConnect extends AbstractMessage implements Control {
    private IntComponent requestID;
    private IntComponent version;

    /**
       An SKConnect with no version number or request ID.
    */
    public SKConnect() {
        super("SK_CONNECT", ControlMessageConstants.SK_CONNECT);
        requestID = new IntComponent("Request ID");
        version = new IntComponent("Version");
        addMessageComponent(requestID);
        addMessageComponent(version);
    }

    /**
       An SKConnect with a given version and request ID.
       @param version The version number.
       @param requestID The request ID.
    */
    public SKConnect(int requestID, int version) {
        this();
        this.requestID.setValue(requestID);
        this.version.setValue(version);
    }

    /**
       Get the version number of this request.
       @return The version number.
    */
    public int getVersion() {
        return version.getValue();
    }

    /**
       Get the request ID.
       @return The request ID.
     */
    public int getRequestID() {
        return requestID.getValue();
    }
}