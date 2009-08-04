package rescuecore2.standard.messages;

/**
   Constants defining message IDs.
 */
public final class MessageConstants {
    /** Kernel-Agent hear (say). */
    public static final int KA_HEAR_SAY = 0x46;
    /** Kernel-Agent hear (tell). */
    public static final int KA_HEAR_TELL = 0x47;

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

    /** Channel subscribe command. */
    public static final int AK_SUBSCRIBE = 0x90;

    /** Channel speak command. */
    public static final int AK_SPEAK = 0x91;

    /** Channel hear command. */
    public static final int KA_HEAR_CHANNEL = 0x92;

    private MessageConstants() {}
}