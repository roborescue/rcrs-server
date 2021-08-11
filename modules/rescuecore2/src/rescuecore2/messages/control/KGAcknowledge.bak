package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;

import java.io.InputStream;
import java.io.IOException;

/**
   A message for acknowleding a connection to the GIS.
 */
public class KGAcknowledge extends AbstractMessage implements Control {
    /**
       A KGAcknowledge message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public KGAcknowledge(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       A KGAcknowldge message.
     */
    public KGAcknowledge() {
        super(ControlMessageURN.KG_ACKNOWLEDGE);
    }
}
