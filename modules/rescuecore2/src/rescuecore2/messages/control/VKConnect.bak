package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.components.StringComponent;

import java.io.InputStream;
import java.io.IOException;

/**
   A message for connecting a viewer to the kernel.
 */
public class VKConnect extends AbstractMessage implements Control {
    private IntComponent requestID;
    private IntComponent version;
    private StringComponent viewerName;

    /**
       A VKConnect message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public VKConnect(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       A VKConnect with a given version and request ID.
       @param version The version number.
       @param requestID The request ID.
       @param name The name of the simulator.
    */
    public VKConnect(int requestID, int version, String name) {
        this();
        this.requestID.setValue(requestID);
        this.version.setValue(version);
        this.viewerName.setValue(name);
    }

    private VKConnect() {
        super(ControlMessageURN.VK_CONNECT);
        requestID = new IntComponent("Request ID");
        version = new IntComponent("Version");
        viewerName = new StringComponent("Name");
        addMessageComponent(requestID);
        addMessageComponent(version);
        addMessageComponent(viewerName);
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
       Get the viewer name.
       @return The name of the viewer.
    */
    public String getViewerName() {
        return viewerName.getValue();
    }
}
