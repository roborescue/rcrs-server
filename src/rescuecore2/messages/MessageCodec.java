package rescuecore2.messages;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Collection;

/**
   This class is responsible for turning messages into byte arrays and vice versa.
 */
public interface MessageCodec {
    /**
       Write a message to an output stream.
       @param m The Message to encode.
       @param out The stream to write it to.
       @throws IOException If writing to the stream fails.
     */
    void encode(Message m, OutputStream out) throws IOException;

    /**
       Write a set of messages to an output stream.
       @param m The Messages to encode.
       @param out The stream to write them to.
       @throws IOException If writing to the stream fails.
     */
    void encode(Collection<Message> m, OutputStream out) throws IOException;

    /**
       Read a message from an input stream.
       @param in The stream to read.
       @return A new Message object, or null if the stream contains no further messages.
       @throws IOException If reading from the stream fails.
     */
    Message decode(InputStream in) throws IOException;
}