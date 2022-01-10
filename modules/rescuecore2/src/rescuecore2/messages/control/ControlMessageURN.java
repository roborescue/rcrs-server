package rescuecore2.messages.control;

import static rescuecore2.Constants.CONTROL_MSG_URN_PREFIX;

import java.util.Map;

import rescuecore2.URN;

/**
 * URNs for control messages.
 */
public enum ControlMessageURN implements URN {
  /** Kernel-GIS connect. */
  KG_CONNECT(CONTROL_MSG_URN_PREFIX | 1, "urn:rescuecore2:messages.control:kg_connect"),
  /** Kernel-GIS acknowledge. */
  KG_ACKNOWLEDGE(CONTROL_MSG_URN_PREFIX | 2, "urn:rescuecore2:messages.control:kg_acknowledge"),
  /** GIS-Kernel OK. */
  GK_CONNECT_OK(CONTROL_MSG_URN_PREFIX | 3, "urn:rescuecore2:messages.control:gk_connect_ok"),
  /** GIS-Kernel error. */
  GK_CONNECT_ERROR(CONTROL_MSG_URN_PREFIX | 4, "urn:rescuecore2:messages.control:gk_connect_error"),
  /** Simulator-Kernel connect. */
  SK_CONNECT(CONTROL_MSG_URN_PREFIX | 5, "urn:rescuecore2:messages.control:sk_connect"),
  /** Simulator-Kernel acknowledge. */
  SK_ACKNOWLEDGE(CONTROL_MSG_URN_PREFIX | 6, "urn:rescuecore2:messages.control:sk_acknowledge"),
  /** Simulator-Kernel update. */
  SK_UPDATE(CONTROL_MSG_URN_PREFIX | 7, "urn:rescuecore2:messages.control:sk_update"),
  /** Kernel-Simulator OK. */
  KS_CONNECT_OK(CONTROL_MSG_URN_PREFIX | 8, "urn:rescuecore2:messages.control:ks_connect_ok"),
  /** Kernel-Simulator error. */
  KS_CONNECT_ERROR(CONTROL_MSG_URN_PREFIX | 9, "urn:rescuecore2:messages.control:ks_connect_error"),
  /** Kernel update broadcast. */
  KS_UPDATE(CONTROL_MSG_URN_PREFIX | 10, "urn:rescuecore2:messages.control:ks_update"),
  /** Kernel commands broadcast. */
  KS_COMMANDS(CONTROL_MSG_URN_PREFIX | 11, "urn:rescuecore2:messages.control:ks_commands"),
  /** Kernel commands aftershocks info. */
  KS_AFTERSHOCKS_INFO(CONTROL_MSG_URN_PREFIX | 12, "urn:rescuecore2:messages.control:ks_aftershocks_info"),

  /** Viewer-Kernel connect. */
  VK_CONNECT(CONTROL_MSG_URN_PREFIX | 13, "urn:rescuecore2:messages.control:vk_connect"),
  /** Viewer-Kernel acknowledge. */
  VK_ACKNOWLEDGE(CONTROL_MSG_URN_PREFIX | 14, "urn:rescuecore2:messages.control:vk_acknowledge"),
  /** Kernel-Viewer OK. */
  KV_CONNECT_OK(CONTROL_MSG_URN_PREFIX | 15, "urn:rescuecore2:messages.control:kv_connect_ok"),
  /** Kernel-Viewer error. */
  KV_CONNECT_ERROR(CONTROL_MSG_URN_PREFIX | 16, "urn:rescuecore2:messages.control:kv_connect_error"),
  /** Kernel-Viewer timestep. */
  KV_TIMESTEP(CONTROL_MSG_URN_PREFIX | 17, "urn:rescuecore2:messages.control:kv_timestep"),

  /** Agent-Kernel connect. */
  AK_CONNECT(CONTROL_MSG_URN_PREFIX | 18, "urn:rescuecore2:messages.control:ak_connect"),
  /** Agent-Kernel acknowledge. */
  AK_ACKNOWLEDGE(CONTROL_MSG_URN_PREFIX | 19, "urn:rescuecore2:messages.control:ak_acknowledge"),
  /** Kernel-Agent OK. */
  KA_CONNECT_OK(CONTROL_MSG_URN_PREFIX | 20, "urn:rescuecore2:messages.control:ka_connect_ok"),
  /** Kernel-Agent error. */
  KA_CONNECT_ERROR(CONTROL_MSG_URN_PREFIX | 21, "urn:rescuecore2:messages.control:ka_connect_error"),
  /** Kernel-Agent perception update. */
  KA_SENSE(CONTROL_MSG_URN_PREFIX | 22, "urn:rescuecore2:messages.control:ka_sense"),

  /** Shutdown message. */
  SHUTDOWN(CONTROL_MSG_URN_PREFIX | 23, "urn:rescuecore2:messages.control:shutdown"),
  /** New EntityID request. */
  ENTITY_ID_REQUEST(CONTROL_MSG_URN_PREFIX | 24, "urn:rescuecore2:messages.control:entity_id_request"),
  /** New EntityID response. */
  ENTITY_ID_RESPONSE(CONTROL_MSG_URN_PREFIX | 25, "urn:rescuecore2:messages.control:entity_id_response");

  private int urnId;
  private String urnStr;
  public static final Map<Integer, ControlMessageURN> MAP = URN.generateMap(ControlMessageURN.class);
  public static final Map<String, ControlMessageURN> MAPSTR = URN.generateMapStr(ControlMessageURN.class);

  private ControlMessageURN(int urnId, String urnStr) {
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

  /**
   * Convert a String to a ControlMessageURN.
   *
   * @param s The String to convert.
   * @return A ControlMessageURN.
   */
  public static ControlMessageURN fromInt(int s) {
    return MAP.get(s);
  }

  public static ControlMessageURN fromString(String urn) {
    return MAPSTR.get(urn);
  }
}