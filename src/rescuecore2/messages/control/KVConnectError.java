package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.StringComponent;
import rescuecore2.messages.IntComponent;

/**
   A message for signalling an unsuccessful connection to the kernel.
 */
public class KVConnectError extends AbstractMessage implements Control {
    private IntComponent requestID;
    private StringComponent reason;

    /**
       A KVConnectError with no reason.
     */
    public KVConnectError() {
        super("KV_CONNECT_ERROR", ControlMessageConstants.KV_CONNECT_ERROR);
        requestID = new IntComponent("Request ID");
        reason = new StringComponent("Reason");
        addMessageComponent(requestID);
        addMessageComponent(reason);
    }

    /**
       A KVConnectError with specified request ID and reason.
       @param requestID The request ID.
       @param message The reason for the error.
     */
    public KVConnectError(int requestID, String message) {
        this();
        this.requestID.setValue(requestID);
        reason.setValue(message);
    }

    /**
       Get the reason for the error.
       @return The reason for the error.
     */
    public String getReason() {
        return reason.getValue();
    }

    /**
       Get the request ID.
       @return The request ID.
     */
    public int getRequestID() {
        return requestID.getValue();
    }
}