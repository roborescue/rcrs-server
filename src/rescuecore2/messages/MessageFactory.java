package rescuecore2.messages;

import java.io.InputStream;
import java.io.IOException;

/**
   Factory class for creating messages.
 */
public interface MessageFactory {
    /**
       Create a message based on its type ID.
       @param id The id of the message type to create.
       @return A new Message object.
     */
    //    Message createMessage(int id);

    /**
       Create a message based on its type ID and populate it with data from a stream. If the ID is not recognised then return null.
       @param id The id of the message type to create.
       @param data An InputStream to read message data from.
       @return A new Message object, or null if the ID is not recognised.
       @throws IOException If there is a problem reading the stream.
     */
    Message createMessage(int id, InputStream data) throws IOException;
}