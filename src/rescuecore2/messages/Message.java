package rescuecore2.messages;

import java.util.List;

/**
   The top-level interface for messages that are sent between simulator components.
 */
public interface Message {
    /**
       Get the name of this message.
       @return The name of the message.
     */
    String getName();

    /**
       Get the ID number of this message type.
       @return The message ID number.
     */
    int getMessageTypeID();

    /**
       Get all the components of this message.
       @return A List of MessageComponent objects.
     */
    List<MessageComponent> getComponents();
}