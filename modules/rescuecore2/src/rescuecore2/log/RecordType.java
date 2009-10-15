package rescuecore2.log;

/**
   Enumeration of possible record types in a log file.
 */
public enum RecordType {
    /** Start of log marker. */
    START_OF_LOG(0xE542D585),
    /** End of log marker. */
    END_OF_LOG(0x00),
    /** Initial conditions record. */
    INITIAL_CONDITIONS(0x01),
    /** Agent perception record. */
    PERCEPTION(0x02),
    /** Commands record. */
    COMMANDS(0x03),
    /** Updates record. */
    UPDATES(0x04),
    /** Config record. */
    CONFIG(0x05);

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
