package rescuecore2.messages.legacy;

import rescuecore2.messages.Message;

/**
   Abstract base class for all legacy messages.
 */
public abstract class LegacyMessage implements Message {
    private MessageType type;

    /**
       Construct a LegacyMessage with a given type.
       @param type The type of message.
     */
    protected LegacyMessage(MessageType type) {
	this.type = type;
    }

    /**
       Get the type of this message.
       @return The message type.
     */
    public MessageType getType() {
	return type;
    }
}