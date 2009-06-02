package rescuecore2.version0.messages;

/**
   Constants defining message IDs.
 */
public final class MessageConstants {
    /** Agent-Kernel connect. */
    public static final int AK_CONNECT = 0x40;
    /** Agent-Kernel acknowledge. */
    public static final int AK_ACKNOWLEDGE = 0x41;
    /** Kernel-Agent OK. */
    public static final int KA_CONNECT_OK = 0x42;
    /** Kernel-Agent error. */
    public static final int KA_CONNECT_ERROR = 0x43;
    /** Kernel-Agent perception update. */
    public static final int KA_SENSE = 0x44;
    /** Kernel-Agent hear (say). */
    public static final int KA_HEAR_SAY = 0x46;
    /** Kernel-Agent hear (tell). */
    public static final int KA_HEAR_TELL = 0x47;

    /** Kernel update broadcast. */
    public static final int UPDATE = 0x50;
    /** Kernel commands broadcast. */
    public static final int COMMANDS = 0x51;

    /** Move command. */
    public static final int AK_MOVE = 0x81;
    /** Load command. */
    public static final int AK_LOAD = 0x82;
    /** Unload command. */
    public static final int AK_UNLOAD = 0x83;
    /** Say command. */
    public static final int AK_SAY = 0x84;
    /** Tell command. */
    public static final int AK_TELL = 0x85;
    /** Extinguish command. */
    public static final int AK_EXTINGUISH = 0x86;
    /** Rescue command. */
    public static final int AK_RESCUE = 0x88;
    /** Clear command. */
    public static final int AK_CLEAR = 0x89;

    private MessageConstants() {}
}