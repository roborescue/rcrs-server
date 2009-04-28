package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;

/**
   A message for signalling an unsuccessful connection to the GIS.
 */
public class GKConnectError extends AbstractMessage {
    private StringComponent reason;

    /**
       A GKConnectError with no reason.
     */
    public GKConnectError() {
        super("GK_CONNECT_ERROR", MessageConstants.GK_CONNECT_ERROR);
        reason = new StringComponent("Reason");
        addMessageComponent(reason);
    }

    /**
       A GKConnectError with a specified reason.
       @param reason The reason for the error.
     */
    public GKConnectError(String reason) {
        this();
        this.reason.setValue(reason);
    }

    /**
       Get the reason for the error.
       @param The reason for the error.
     */
    public String getReason() {
        return reason.getValue();
    }
}