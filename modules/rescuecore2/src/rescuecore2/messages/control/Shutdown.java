package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;

import java.io.InputStream;
import java.io.IOException;

/**
   A message from the kernel indicating that components should shut down.
*/
public class Shutdown extends AbstractMessage implements Control {
    /**
       Construct a Shutdown message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public Shutdown(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       Construct a Shutdown message.
     */
    public Shutdown() {
        super(ControlMessageURN.SHUTDOWN);
    }
}
