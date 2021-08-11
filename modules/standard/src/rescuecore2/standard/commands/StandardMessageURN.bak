package rescuecore2.standard.messages;

import static rescuecore2.standard.Constants.MESSAGE_URN_PREFIX;

/**
   URNs for standard messages.
 */
public enum StandardMessageURN {
    /** Rest command. */
    AK_REST(MESSAGE_URN_PREFIX + "rest"),
    /** Move command. */
    AK_MOVE(MESSAGE_URN_PREFIX + "move"),
    /** Load command. */
    AK_LOAD(MESSAGE_URN_PREFIX + "load"),
    /** Unload command. */
    AK_UNLOAD(MESSAGE_URN_PREFIX + "unload"),
    /** Say command. */
    AK_SAY(MESSAGE_URN_PREFIX + "say"),
    /** Tell command. */
    AK_TELL(MESSAGE_URN_PREFIX + "tell"),
    /** Extinguish command. */
    AK_EXTINGUISH(MESSAGE_URN_PREFIX + "extinguish"),
    /** Rescue command. */
    AK_RESCUE(MESSAGE_URN_PREFIX + "rescue"),
    /** Clear command. */
    AK_CLEAR(MESSAGE_URN_PREFIX + "clear"),
    /** Clear-Area command. */
    AK_CLEAR_AREA(MESSAGE_URN_PREFIX + "clear_area"),

    /** Channel subscribe command. */
    AK_SUBSCRIBE(MESSAGE_URN_PREFIX + "subscribe"),

    /** Channel speak command. */
    AK_SPEAK(MESSAGE_URN_PREFIX + "speak");


    private String urn;

    private StandardMessageURN(String urn) {
        this.urn = urn;
    }

    @Override
    public String toString() {
        return urn;
    }

    /**
       Convert a String to a StandardMessageURN.
       @param s The String to convert.
       @return A StandardMessageURN.
    */
    public static StandardMessageURN fromString(String s) {
        for (StandardMessageURN next : StandardMessageURN.values()) {
            if (next.urn.equals(s)) {
                return next;
            }
        }
        throw new IllegalArgumentException(s);
    }
}
