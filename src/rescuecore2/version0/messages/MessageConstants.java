package rescuecore2.version0.messages;

/**
   Constants defining message IDs.
 */
public final class MessageConstants {
    /** Kernel-GIS connect. */
    public static final int KG_CONNECT = 0x10;
    /** Kernel-GIS acknowledge. */
    public static final int KG_ACKNOWLEDGE = 0x11;
    /** GIS-Kernel OK. */
    public static final int GK_CONNECT_OK = 0x12;
    /** GIS-Kernel error. */
    public static final int GK_CONNECT_ERROR = 0x13;

    /** Agent-Kernel connect. */
    public static final int AK_CONNECT = 0x40;
    /** Agent-Kernel acknowldge. */
    public static final int AK_ACKNOWLEDGE = 0x41;
    /** Kernel-Agent OK. */
    public static final int KA_CONNECT_OK = 0x42;
    /** Kernel-Agent error. */
    public static final int KA_CONNECT_ERROR = 0x43;

    private MessageConstants() {}
}