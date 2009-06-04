package rescuecore2.messages.control;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.StringComponent;

/**
   A message for signalling an unsuccessful connection to the kernel.
 */
public class KAConnectError extends AbstractMessage {
    private IntComponent requestID;
    private StringComponent reason;

    /**
       A KAConnectError with no tempID or reason.
     */
    public KAConnectError() {
        super("KA_CONNECT_ERROR", ControlMessageConstants.KA_CONNECT_ERROR);
        requestID = new IntComponent("Request ID");
        reason = new StringComponent("Reason");
        addMessageComponent(requestID);
        addMessageComponent(reason);
    }

    /**
       A KAConnectError with specified request ID and reason.
       @param id The ID of the request that failed.
       @param message The reason for the error.
     */
    public KAConnectError(int id, String message) {
        this();
        requestID.setValue(id);
        reason.setValue(message);
    }

    /**
       Get the request ID for the message.
       @return The request ID for the message.
     */
    public int getRequestID() {
        return requestID.getValue();
    }

    /**
       Get the reason for the error.
       @return The reason for the error.
     */
    public String getReason() {
        return reason.getValue();
    }
}