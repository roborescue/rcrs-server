package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.version0.messages.StringComponent;

/**
   A message for signalling an unsuccessful connection to the GIS.
 */
public class GKConnectError extends AbstractMessage {
    private StringComponent reason;

    public GKConnectError() {
        super("GK_CONNECT_ERROR", MessageConstants.GK_CONNECT_ERROR);
        reason = new StringComponent("Reason");
        addMessageComponent(reason);
    }

    /**
       Get the reason for the error.
       @param The reason for the error.
     */
    public String getReason() {
        return reason.getValue();
    }
}