package rescuecore2.standard.messages;

/**
   URNs for standard messages.
 */
public enum StandardMessageURN {
    /** Kernel-Agent hear (say). */
    KA_HEAR_SAY,
    /** Kernel-Agent hear (tell). */
    KA_HEAR_TELL,

    /** Move command. */
    AK_MOVE,
    /** Load command. */
    AK_LOAD,
    /** Unload command. */
    AK_UNLOAD,
    /** Say command. */
    AK_SAY,
    /** Tell command. */
    AK_TELL,
    /** Extinguish command. */
    AK_EXTINGUISH,
    /** Rescue command. */
    AK_RESCUE,
    /** Clear command. */
    AK_CLEAR,

    /** Channel subscribe command. */
    AK_SUBSCRIBE,

    /** Channel speak command. */
    AK_SPEAK,

    /** Channel hear command. */
    KA_HEAR_CHANNEL;
}