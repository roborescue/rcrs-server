package rescuecore2.messages.legacy;

/**
   The different types of legacy message.
 */
public enum MessageType {
    NULL(0),
	KG_CONNECT(0x10),
	KG_ACKNOWLEDGE(0x11),
	GK_CONNECT_OK(0x12),
	GK_CONNECT_ERROR(0x13);

    private int id;

    private MessageType(int id) {
	this.id = id;
    }

    /**
       Get the ID number of this message type.
       @return The ID of this message type.
     */
    public int getID() {
	return id;
    }

    /**
       Look up a message type by ID.
       @param id The ID to look up.
       @return The appropriate MessageType object.
       @throws IllegalArgumentException If the given id is not recognised.
     */
    public static MessageType fromID(int id) {
	for (MessageType next : MessageType.values()) {
	    if (next.getID() == id) {
		return next;
	    }
	}
	throw new IllegalArgumentException("Unrecognised message type ID: " + id);
    }
}