package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.IntListComponent;

import java.util.List;

import java.io.InputStream;
import java.io.IOException;

/**
   A message for connecting an agent to the kernel.
 */
public class AKConnect extends AbstractMessage implements Control {
    private IntComponent requestID;
    private IntComponent version;
    private IntListComponent requestedEntityTypes;

    /**
       An AKConnect message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public AKConnect(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       An AKConnect with particular version, requestID and requested entity types.
       @param version The version number.
       @param requestID The request ID.
       @param requestedEntityTypes The set of requested entity types.
     */
    public AKConnect(int requestID, int version, int... requestedEntityTypes) {
        this();
        this.requestID.setValue(requestID);
        this.version.setValue(version);
        this.requestedEntityTypes.setValues(requestedEntityTypes);
    }

    private AKConnect() {
        super("AK_CONNECT", ControlMessageConstants.AK_CONNECT);
        requestID = new IntComponent("Request ID");
        version = new IntComponent("Version");
        requestedEntityTypes = new IntListComponent("Requested entity types");
        addMessageComponent(requestID);
        addMessageComponent(version);
        addMessageComponent(requestedEntityTypes);
    }

    /**
       Get the version number of this request.
       @return The version number.
     */
    public int getVersion() {
        return version.getValue();
    }

    /**
       Get the request ID.
       @return The request ID.
     */
    public int getRequestID() {
        return requestID.getValue();
    }

    /**
       Get the requested entity types.
       @return The requested entity types.
     */
    public List<Integer> getRequestedEntityTypes() {
        return requestedEntityTypes.getValues();
    }
}