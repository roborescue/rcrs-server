package rescuecore2.standard.entities;

import static rescuecore2.standard.Constants.ENTITY_URN_PREFIX;
import static rescuecore2.standard.Constants.ENTITY_URN_PREFIX_STR;

import java.util.Map;

import rescuecore2.URN;

/**
 * URNs for standard entities.
 */
public enum StandardEntityURN implements URN {
  /** World entity */
  WORLD(ENTITY_URN_PREFIX | 1, ENTITY_URN_PREFIX_STR + "world"),
  /** Road entity */
  ROAD(ENTITY_URN_PREFIX | 2, ENTITY_URN_PREFIX_STR + "road"),
  /** Blockade entity */
  BLOCKADE(ENTITY_URN_PREFIX | 3, ENTITY_URN_PREFIX_STR + "blockade"),
  /** Building entity */
  BUILDING(ENTITY_URN_PREFIX | 4, ENTITY_URN_PREFIX_STR + "building"),
  /** Refuge entity */
  REFUGE(ENTITY_URN_PREFIX | 5, ENTITY_URN_PREFIX_STR + "refuge"),
  /** Hydrant entity */
  HYDRANT(ENTITY_URN_PREFIX | 6, ENTITY_URN_PREFIX_STR + "hydrant"),
  /** Gas Station entity */
  GAS_STATION(ENTITY_URN_PREFIX | 7, ENTITY_URN_PREFIX_STR + "gasstation"),
  /** Fire Station entity */
  FIRE_STATION(ENTITY_URN_PREFIX | 8, ENTITY_URN_PREFIX_STR + "firestation"),
  /** Ambulance Centre entity */
  AMBULANCE_CENTRE(ENTITY_URN_PREFIX | 9, ENTITY_URN_PREFIX_STR + "ambulancecentre"),
  /** Police Office entity */
  POLICE_OFFICE(ENTITY_URN_PREFIX | 10, ENTITY_URN_PREFIX_STR + "policeoffice"),
  /** Civilian entity */
  CIVILIAN(ENTITY_URN_PREFIX | 11, ENTITY_URN_PREFIX_STR + "civilian"),
  /** Fire Brigade entity */
  FIRE_BRIGADE(ENTITY_URN_PREFIX | 12, ENTITY_URN_PREFIX_STR + "firebrigade"),
  /** Ambulance Team entity */
  AMBULANCE_TEAM(ENTITY_URN_PREFIX | 13, ENTITY_URN_PREFIX_STR + "ambulanceteam"),
  /** Police Force entity */
  POLICE_FORCE(ENTITY_URN_PREFIX | 14, ENTITY_URN_PREFIX_STR + "policeforce");

  private int urnId;
  private String urnStr;
  public static final Map<Integer, StandardEntityURN> MAP = URN.generateMap(StandardEntityURN.class);
  public static final Map<String, StandardEntityURN> MAPSTR = URN.generateMapStr(StandardEntityURN.class);

  private StandardEntityURN(int urnId, String urnStr) {
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

  public static StandardEntityURN fromInt(int urn) {
    return MAP.get(urn);
  }

  public static StandardEntityURN fromString(String urn) {
    return MAPSTR.get(urn);
  }
}