package rescuecore2.messages;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
   A piece of a message.
 */
public interface MessageComponent {
    /**
       Get the name of this component of the message.
       @return The name of this component.
     */
    String getName();

    /**
       Write this component to a stream.
       @param out The stream to write to.
       @throws IOException If the write fails.
     */
    void write(OutputStream out) throws IOException;

    /**
       Read this component from a stream.
       @param in The stream to read from.
       @throws IOException If the read fails.
     */
    void read(InputStream in) throws IOException;
}
