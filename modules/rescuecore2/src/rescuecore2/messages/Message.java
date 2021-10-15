package rescuecore2.messages;

import java.io.InputStream;
import java.io.OutputStream;

import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

import java.io.IOException;

/**
   The top-level interface for messages that are sent between simulator components.
 */
public interface Message {
    /**
       Get the urn of this message type.
       @return The message urn.
     */
    int getURN();

    /**
       Write the content of this message to a stream. The content should not include the message type ID.
       @param out The stream to write to.
       @throws IOException If the write fails.
     */
    void write(OutputStream out) throws IOException;

    /**
       Read the content of this message from a stream. The content should not include the message type ID.
       @param in The stream to read from.
       @throws IOException If the read fails.
     */
    void read(InputStream in) throws IOException;
    
    
    MessageProto toMessageProto();
    void fromMessageProto(MessageProto proto);
}
