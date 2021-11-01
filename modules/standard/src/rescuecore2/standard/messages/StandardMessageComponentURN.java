package rescuecore2.standard.messages;

import static rescuecore2.standard.Constants.STANDARD_MSG_COMPONENT_URN_PREFIX;

import java.util.Map;

import rescuecore2.URN;;

public enum StandardMessageComponentURN implements URN {
  Target(STANDARD_MSG_COMPONENT_URN_PREFIX | 1, "Target"),
  DestinationX(STANDARD_MSG_COMPONENT_URN_PREFIX | 2, "Destination X"),
  DestinationY(STANDARD_MSG_COMPONENT_URN_PREFIX | 3, "Destination Y"),
  Water(STANDARD_MSG_COMPONENT_URN_PREFIX | 4, "Water"), Path(STANDARD_MSG_COMPONENT_URN_PREFIX | 5, "Path"),
  Message(STANDARD_MSG_COMPONENT_URN_PREFIX | 6, "Message"), Channel(STANDARD_MSG_COMPONENT_URN_PREFIX | 7, "Channel"),
  Channels(STANDARD_MSG_COMPONENT_URN_PREFIX | 8, "Channels");

  private int urnId;
  private String urnStr;
  public static final Map<Integer, StandardMessageComponentURN> MAP = URN
      .generateMap(StandardMessageComponentURN.class);
  public static final Map<String, StandardMessageComponentURN> MAPSTR = URN
      .generateMapStr(StandardMessageComponentURN.class);

  StandardMessageComponentURN(int urnId, String urnStr) {
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

  public static StandardMessageComponentURN fromInt(int urn) {
    return MAP.get(urn);
  }

  public static StandardMessageComponentURN fromString(String urn) {
    return MAPSTR.get(urn);
  }
}