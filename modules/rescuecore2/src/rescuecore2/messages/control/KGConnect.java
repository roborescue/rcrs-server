package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.IntComponent;

import java.io.InputStream;
import java.io.IOException;

/**
   A message for connecting to the GIS.
 */
public class KGConnect extends AbstractMessage implements Control {
    private IntComponent version;

    /**
       A KGConnect message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public KGConnect(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       A KGConnect message with a specified version number.
       @param version The version number field.
     */
    public KGConnect(int version) {
        this();
        this.version.setValue(version);
    }

    private KGConnect() {
        super(ControlMessageURN.KG_CONNECT);
        version = new IntComponent("Version", 0);
        addMessageComponent(version);
    }

    /**
       Get the version number of the message.
       @return The version number field.
    */
    public int getVersion() {
        return version.getValue();
    }
}
