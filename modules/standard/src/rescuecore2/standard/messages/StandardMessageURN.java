package rescuecore2.standard.messages;

import static rescuecore2.standard.Constants.MESSAGE_URN_PREFIX;
import static rescuecore2.standard.Constants.MESSAGE_URN_PREFIX_STR;

import java.util.Map;

import rescuecore2.URN;

/**
 * URNs for standard messages.
 */
public enum StandardMessageURN implements URN {
  /** Rest command. */
  AK_REST(MESSAGE_URN_PREFIX | 1, MESSAGE_URN_PREFIX_STR + "rest"),
  /** Move command. */
  AK_MOVE(MESSAGE_URN_PREFIX | 2, MESSAGE_URN_PREFIX_STR + "move"),
  /** Load command. */
  AK_LOAD(MESSAGE_URN_PREFIX | 3, MESSAGE_URN_PREFIX_STR + "load"),
  /** Unload command. */
  AK_UNLOAD(MESSAGE_URN_PREFIX | 4, MESSAGE_URN_PREFIX_STR + "unload"),
  /** Say command. */
  AK_SAY(MESSAGE_URN_PREFIX | 5, MESSAGE_URN_PREFIX_STR + "say"),
  /** Tell command. */
  AK_TELL(MESSAGE_URN_PREFIX | 6, MESSAGE_URN_PREFIX_STR + "tell"),
  /** Extinguish command. */
  AK_EXTINGUISH(MESSAGE_URN_PREFIX | 7, MESSAGE_URN_PREFIX_STR + "extinguish"),
  /** Rescue command. */
  AK_RESCUE(MESSAGE_URN_PREFIX | 8, MESSAGE_URN_PREFIX_STR + "rescue"),
  /** Clear command. */
  AK_CLEAR(MESSAGE_URN_PREFIX | 9, MESSAGE_URN_PREFIX_STR + "clear"),
  /** Clear-Area command. */
  AK_CLEAR_AREA(MESSAGE_URN_PREFIX | 10, MESSAGE_URN_PREFIX_STR + "clear_area"),
  /** Channel subscribe command. */
  AK_SUBSCRIBE(MESSAGE_URN_PREFIX | 11, MESSAGE_URN_PREFIX_STR + "subscribe"),
  /** Channel speak command. */
  AK_SPEAK(MESSAGE_URN_PREFIX | 12, MESSAGE_URN_PREFIX_STR + "speak");

  private int urnId;
  private String urnStr;
  public static final Map<Integer, StandardMessageURN> MAP = URN.generateMap(StandardMessageURN.class);
  public static final Map<String, StandardMessageURN> MAPSTR = URN.generateMapStr(StandardMessageURN.class);

  private StandardMessageURN(int urnId, String urnStr) {
    this.urnId = urnId;
    this.urnStr = urnStr;
  }

  @Override
  public int getURNId() {
    return this.urnId;
  }

  @Override
  public String getURNStr() {
    return this.urnStr;
  }

  /**
   * Convert a String to a StandardMessageURN.
   *
   * @param s The String to convert.
   * @return A StandardMessageURN.
   */
  public static StandardMessageURN fromInt(int s) {
    return MAP.get(s);
  }

  public static StandardMessageURN fromString(int s) {
    return MAP.get(s);
  }
}