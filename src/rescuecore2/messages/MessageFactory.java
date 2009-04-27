package rescuecore2.messages;

/**
   Factory class for creating messages.
 */
public interface MessageFactory {
    /**
       Create a message based on its type ID.
       @param id The id of the message type to create.
       @return A new Message object.
     */
    Message createMessage(int id);
}