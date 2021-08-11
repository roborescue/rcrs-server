package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.StringComponent;
import rescuecore2.messages.components.IntComponent;

import java.io.InputStream;
import java.io.IOException;

/**
   A message for signalling an unsuccessful connection to the kernel.
 */
public class KSConnectError extends AbstractMessage implements Control {
    private IntComponent requestID;
    private StringComponent reason;

    /**
       A KSConnectError message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public KSConnectError(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       A KSConnectError with specified request ID and reason.
       @param requestID The request ID.
       @param message The reason for the error.
    */
    public KSConnectError(int requestID, String message) {
        this();
        this.requestID.setValue(requestID);
        reason.setValue(message);
    }

    private KSConnectError() {
        super(ControlMessageURN.KS_CONNECT_ERROR);
        requestID = new IntComponent("Request ID");
        reason = new StringComponent("Reason");
        addMessageComponent(requestID);
        addMessageComponent(reason);
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
