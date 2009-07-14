package kernel.log;

/**
   Enumeration of possible record types in a kernel log.
 */
public enum RecordType {
    START_OF_LOG(0xE542D585),
    END_OF_LOG(0x00),
    INITIAL_CONDITIONS(0x01),
    PERCEPTION(0x02),
    COMMANDS(0x03),
    UPDATES(0x04);

    private int id;

    private RecordType(int id) {
        this.id = id;
    }

    /**
       Get the ID of this record type for writing into the log.
       @return The 32-bit ID of this record type.
     */
    public int getID() {
        return id;
    }

    /**
       Get the RecordType representing a type ID.
       @param id The ID to look up.
       @return A RecordType object.
       @throws IllegalArgumentException If the id is invalid.
     */
    public static RecordType fromID(int id) {
        for (RecordType next : values()) {
            if (next.id == id) {
                return next;
            }
        }
        throw new IllegalArgumentException("Unrecognised RecordType: " + id);
    }
}
