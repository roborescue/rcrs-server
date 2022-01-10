package rescuecore2.standard.entities;

import static rescuecore2.standard.Constants.PROPERTY_URN_PREFIX;
import static rescuecore2.standard.Constants.PROPERTY_URN_PREFIX_STR;

import java.util.Map;

import rescuecore2.URN;;

/**
 * URNs for standard property types.
 */
public enum StandardPropertyURN implements URN {
  /** Start Time property */
  START_TIME(PROPERTY_URN_PREFIX | 1, PROPERTY_URN_PREFIX_STR + "starttime"),
  /** Longitude property */
  LONGITUDE(PROPERTY_URN_PREFIX | 2, PROPERTY_URN_PREFIX_STR + "longitude"),
  /** Latitude property */
  LATITUDE(PROPERTY_URN_PREFIX | 3, PROPERTY_URN_PREFIX_STR + "latitude"),
  /** Wind Force property */
  WIND_FORCE(PROPERTY_URN_PREFIX | 4, PROPERTY_URN_PREFIX_STR + "windforce"),
  /** Wind Direction property */
  WIND_DIRECTION(PROPERTY_URN_PREFIX | 5, PROPERTY_URN_PREFIX_STR + "winddirection"),
  /** X property */
  X(PROPERTY_URN_PREFIX | 6, PROPERTY_URN_PREFIX_STR + "x"),
  /** Y property */
  Y(PROPERTY_URN_PREFIX | 7, PROPERTY_URN_PREFIX_STR + "y"),
  /** Blockades property */
  BLOCKADES(PROPERTY_URN_PREFIX | 8, PROPERTY_URN_PREFIX_STR + "blockades"),
  /** Repair Cost property */
  REPAIR_COST(PROPERTY_URN_PREFIX | 9, PROPERTY_URN_PREFIX_STR + "repaircost"),
  /** Floors property */
  FLOORS(PROPERTY_URN_PREFIX | 10, PROPERTY_URN_PREFIX_STR + "floors"),
  /** Building Attributes property */
  BUILDING_ATTRIBUTES(PROPERTY_URN_PREFIX | 11, PROPERTY_URN_PREFIX_STR + "buildingattributes"),
  /** Ignition property */
  IGNITION(PROPERTY_URN_PREFIX | 12, PROPERTY_URN_PREFIX_STR + "ignition"),
  /** Fieryness property */
  FIERYNESS(PROPERTY_URN_PREFIX | 13, PROPERTY_URN_PREFIX_STR + "fieryness"),
  /** Brokeness property */
  BROKENNESS(PROPERTY_URN_PREFIX | 14, PROPERTY_URN_PREFIX_STR + "brokenness"),
  /** Building Code property */
  BUILDING_CODE(PROPERTY_URN_PREFIX | 15, PROPERTY_URN_PREFIX_STR + "buildingcode"),
  /** Building Ground Area property */
  BUILDING_AREA_GROUND(PROPERTY_URN_PREFIX | 16, PROPERTY_URN_PREFIX_STR + "buildingareaground"),
  /** Building Total Area property */
  BUILDING_AREA_TOTAL(PROPERTY_URN_PREFIX | 17, PROPERTY_URN_PREFIX_STR + "buildingareatotal"),
  /** Apexes property */
  APEXES(PROPERTY_URN_PREFIX | 18, PROPERTY_URN_PREFIX_STR + "apexes"),
  /** Edges property */
  EDGES(PROPERTY_URN_PREFIX | 19, PROPERTY_URN_PREFIX_STR + "edges"),
  /** Position property */
  POSITION(PROPERTY_URN_PREFIX | 20, PROPERTY_URN_PREFIX_STR + "position"),
  /** Direction property */
  DIRECTION(PROPERTY_URN_PREFIX | 21, PROPERTY_URN_PREFIX_STR + "direction"),
  /** Position History property */
  POSITION_HISTORY(PROPERTY_URN_PREFIX | 22, PROPERTY_URN_PREFIX_STR + "positionhistory"),
  /** Stamina property */
  STAMINA(PROPERTY_URN_PREFIX | 23, PROPERTY_URN_PREFIX_STR + "stamina"),
  /** HP property */
  HP(PROPERTY_URN_PREFIX | 24, PROPERTY_URN_PREFIX_STR + "hp"),
  /** Damage property */
  DAMAGE(PROPERTY_URN_PREFIX | 25, PROPERTY_URN_PREFIX_STR + "damage"),
  /** Buriedness property */
  BURIEDNESS(PROPERTY_URN_PREFIX | 26, PROPERTY_URN_PREFIX_STR + "buriedness"),
  /** Travel Distance property */
  TRAVEL_DISTANCE(PROPERTY_URN_PREFIX | 27, PROPERTY_URN_PREFIX_STR + "traveldistance"),
  /** Water Quantity property */
  WATER_QUANTITY(PROPERTY_URN_PREFIX | 28, PROPERTY_URN_PREFIX_STR + "waterquantity"),
  /** Temperature property */
  TEMPERATURE(PROPERTY_URN_PREFIX | 29, PROPERTY_URN_PREFIX_STR + "temperature"),
  /** Importance property */
  IMPORTANCE(PROPERTY_URN_PREFIX | 30, PROPERTY_URN_PREFIX_STR + "importance"),
  /** Capacity property */
  CAPACITY(PROPERTY_URN_PREFIX | 31, PROPERTY_URN_PREFIX_STR + "capacity"),
  /** Bed Capacity property */
  BED_CAPACITY(PROPERTY_URN_PREFIX | 32, PROPERTY_URN_PREFIX_STR + "bedcapacity"),
  /** Floors property */
  OCCUPIED_BEDS(PROPERTY_URN_PREFIX | 33, PROPERTY_URN_PREFIX_STR + "occupiedbeds"),
  /** Refill Capacity property */
  REFILL_CAPACITY(PROPERTY_URN_PREFIX | 34, PROPERTY_URN_PREFIX_STR + "refillcapacity"),
  /** Waiting List Size property */
  WAITING_LIST_SIZE(PROPERTY_URN_PREFIX | 35, PROPERTY_URN_PREFIX_STR + "waitinglistlize");

  private int urnId;
  private String urnStr;
  public static final Map<Integer, StandardPropertyURN> MAP = URN.generateMap(StandardPropertyURN.class);
  public static final Map<String, StandardPropertyURN> MAPSTR = URN.generateMapStr(StandardPropertyURN.class);

  private StandardPropertyURN(int urnId, String urnStr) {
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
   * Convert a String to a StandardPropertyURN.
   *
   * @param s The String to convert.
   * @return A StandardPropertyURN.
   */
  public static StandardPropertyURN fromInt(int s) {
    return MAP.get(s);
  }

  public static StandardPropertyURN fromString(String s) {
    return MAPSTR.get(s);
  }
}