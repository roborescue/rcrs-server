package rescuecore2.messages.legacy;

/**
   A message the GIS sends to the kernel when there is an error connecting.
 */
public class GKConnectError extends LegacyMessage {
    private String reason;

    /**
       Construct a GKConnectError message.
       @param reason The reason for the error.
     */
    public GKConnectError(String reason) {
	super(MessageType.GK_CONNECT_ERROR);
        this.reason = reason;
    }

    /**
       Get the reason for the error.
       @return The reason for the error.
     */
    public String getReason() {
        return reason;
    }
}