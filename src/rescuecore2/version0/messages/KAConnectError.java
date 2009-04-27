package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.version0.messages.StringComponent;

/**
   A message for signalling an unsuccessful connection to the kernel.
 */
public class KAConnectError extends AbstractMessage {
    private IntComponent tempID;
    private StringComponent reason;

    public KAConnectError() {
        super("KA_CONNECT_ERROR", MessageConstants.KA_CONNECT_ERROR);
        tempID = new IntComponent("TempID");
        reason = new StringComponent("Reason");
        addMessageComponent(tempID);
        addMessageComponent(reason);
    }

    public KAConnectError(int id, String message) {
        this();
        tempID.setValue(id);
        reason.setValue(message);
    }

    /**
       Get the temp ID for the message.
       @return The temp ID for the message.
     */
    public int getTempID() {
        return tempID.getValue();
    }

    /**
       Get the reason for the error.
       @return The reason for the error.
     */
    public String getReason() {
        return reason.getValue();
    }
}