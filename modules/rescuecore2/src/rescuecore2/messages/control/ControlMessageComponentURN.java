package rescuecore2.messages.control;

import static rescuecore2.Constants.CONTROL_MSG_COMPONENT_URN_PREFIX;

import java.util.Map;

import rescuecore2.URN;

public enum ControlMessageComponentURN implements URN {
  RequestID(CONTROL_MSG_COMPONENT_URN_PREFIX | 1, "Request ID"),
  AgentID(CONTROL_MSG_COMPONENT_URN_PREFIX | 2, "Agent ID"), Version(CONTROL_MSG_COMPONENT_URN_PREFIX | 3, "Version"),
  Name(CONTROL_MSG_COMPONENT_URN_PREFIX | 4, "Name"),
  RequestedEntityTypes(CONTROL_MSG_COMPONENT_URN_PREFIX | 5, "Requested entity types"),
  SimulatorID(CONTROL_MSG_COMPONENT_URN_PREFIX | 6, "Simulator ID"),
  RequestNumber(CONTROL_MSG_COMPONENT_URN_PREFIX | 7, "Request number"),
  NumberOfIDs(CONTROL_MSG_COMPONENT_URN_PREFIX | 8, "Number of IDs"),
  NewEntityIDs(CONTROL_MSG_COMPONENT_URN_PREFIX | 9, "New entity IDs"),
  Reason(CONTROL_MSG_COMPONENT_URN_PREFIX | 10, "Reason"), Entities(CONTROL_MSG_COMPONENT_URN_PREFIX | 11, "Entities"),
  ViewerID(CONTROL_MSG_COMPONENT_URN_PREFIX | 12, "ViewerID"),
  AgentConfig(CONTROL_MSG_COMPONENT_URN_PREFIX | 13, "Agent config"),
  Time(CONTROL_MSG_COMPONENT_URN_PREFIX | 14, "Time"), Updates(CONTROL_MSG_COMPONENT_URN_PREFIX | 15, "Updates"),
  Hearing(CONTROL_MSG_COMPONENT_URN_PREFIX | 16, "Hearing"),
  INTENSITIES(CONTROL_MSG_COMPONENT_URN_PREFIX | 17, "INTENSITIES"),
  TIMES(CONTROL_MSG_COMPONENT_URN_PREFIX | 18, "TIMES"), ID(CONTROL_MSG_COMPONENT_URN_PREFIX | 19, "ID"),
  Commands(CONTROL_MSG_COMPONENT_URN_PREFIX | 20, "Commands"),
  SimulatorConfig(CONTROL_MSG_COMPONENT_URN_PREFIX | 21, "Simulator config"),
  Changes(CONTROL_MSG_COMPONENT_URN_PREFIX | 22, "Changes");

  private int urnId;
  private String urnStr;
  public static final Map<Integer, ControlMessageComponentURN> MAP = URN.generateMap(ControlMessageComponentURN.class);
  public static final Map<String, ControlMessageComponentURN> MAPSTR = URN
      .generateMapStr(ControlMessageComponentURN.class);

  ControlMessageComponentURN(int urnId, String urnStr) {
    this.urnId = urnId;
    this.urnStr = urnStr;
  }

  @Override
  public int getURNId() {
    return urnId;
  }

  @Override
  public String getURNStr() {
    return urnStr;
  }

  public static ControlMessageComponentURN fromInt(int urnId) {
    return MAP.get(urnId);
  }

  public static ControlMessageComponentURN fromString(String urnStr) {
    return MAPSTR.get(urnStr);
  }
}