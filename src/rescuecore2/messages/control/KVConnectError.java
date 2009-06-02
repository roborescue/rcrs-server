package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.StringComponent;

/**
   A message for signalling an unsuccessful connection to the kernel.
 */
public class KVConnectError extends AbstractMessage implements Control {
    private StringComponent reason;

    /**
       A KVConnectError with no reason.
     */
    public KVConnectError() {
        super("KV_CONNECT_ERROR", ControlMessageConstants.KV_CONNECT_ERROR);
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