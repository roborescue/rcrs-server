package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.components.StringComponent;

import java.io.InputStream;
import java.io.IOException;

/**
   A message for signalling an unsuccessful connection to the kernel.
 */
public class KAConnectError extends AbstractMessage implements Control {
    private IntComponent requestID;
    private StringComponent reason;

    /**
       A KAConnectError message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public KAConnectError(InputStream in) throws IOException {
        this();
        read(in);
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

    private KAConnectError() {
        super(ControlMessageURN.KA_CONNECT_ERROR);
        requestID = new IntComponent("Request ID");
        reason = new StringComponent("Reason");
        addMessageComponent(requestID);
        addMessageComponent(reason);
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
