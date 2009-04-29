package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;

/**
   A message for signalling an unsuccessful connection to the kernel.
 */
public class KSConnectError extends AbstractMessage {
    private StringComponent reason;

    /**
       A KSConnectError with no reason.
     */
    public KSConnectError() {
        super("KS_CONNECT_ERROR", MessageConstants.KS_CONNECT_ERROR);
        reason = new StringComponent("Reason");
        addMessageComponent(reason);
    }

    /**
       A KSConnectError with specified reason.
       @param message The reason for the error.
     */
    public KSConnectError(String message) {
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