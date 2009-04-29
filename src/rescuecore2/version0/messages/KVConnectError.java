package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;

/**
   A message for signalling an unsuccessful connection to the kernel.
 */
public class KVConnectError extends AbstractMessage {
    private StringComponent reason;

    /**
       A KVConnectError with no reason.
     */
    public KVConnectError() {
        super("KV_CONNECT_ERROR", MessageConstants.KV_CONNECT_ERROR);
        reason = new StringComponent("Reason");
        addMessageComponent(reason);
    }

    /**
       A KVConnectError with specified reason.
       @param message The reason for the error.
     */
    public KVConnectError(String message) {
        this();
        reason.setValue(message);
    }

    /**
       Get the reason for the error.
       @return The reason for the error.
     */
    public String getReason() {
        return reason.getValue();
    }
}