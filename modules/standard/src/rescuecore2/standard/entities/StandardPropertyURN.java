package rescuecore2.standard.entities;

import static rescuecore2.standard.Constants.PROPERTY_URN_PREFIX;

/**
   URNs for standard property types.
 */
public enum StandardPropertyURN {
    // CHECKSTYLE:OFF:JavadocVariableChec

    START_TIME(PROPERTY_URN_PREFIX + "starttime"),
    LONGITUDE(PROPERTY_URN_PREFIX + "longitude"),
    LATITUDE(PROPERTY_URN_PREFIX + "latitude"),
    WIND_FORCE(PROPERTY_URN_PREFIX + "windforce"),
    WIND_DIRECTION(PROPERTY_URN_PREFIX + "winddirection"),

    X(PROPERTY_URN_PREFIX + "x"),
    Y(PROPERTY_URN_PREFIX + "y"),

    BLOCKADES(PROPERTY_URN_PREFIX + "blockades"),
    REPAIR_COST(PROPERTY_URN_PREFIX + "repaircost"),

    FLOORS(PROPERTY_URN_PREFIX + "floors"),
    BUILDING_ATTRIBUTES(PROPERTY_URN_PREFIX + "buildingattributes"),
    IGNITION(PROPERTY_URN_PREFIX + "ignition"),
    FIERYNESS(PROPERTY_URN_PREFIX + "fieryness"),
    BROKENNESS(PROPERTY_URN_PREFIX + "brokenness"),
    BUILDING_CODE(PROPERTY_URN_PREFIX + "buildingcode"),
    BUILDING_AREA_GROUND(PROPERTY_URN_PREFIX + "buildingareaground"),
    BUILDING_AREA_TOTAL(PROPERTY_URN_PREFIX + "buildingareatotal"),
    APEXES(PROPERTY_URN_PREFIX + "apexes"),
    EDGES(PROPERTY_URN_PREFIX + "edges"),

    POSITION(PROPERTY_URN_PREFIX + "position"),
    DIRECTION(PROPERTY_URN_PREFIX + "direction"),
    POSITION_HISTORY(PROPERTY_URN_PREFIX + "positionhistory"),
    STAMINA(PROPERTY_URN_PREFIX + "stamina"),
    HP(PROPERTY_URN_PREFIX + "hp"),
    DAMAGE(PROPERTY_URN_PREFIX + "damage"),
    BURIEDNESS(PROPERTY_URN_PREFIX + "buriedness"),
    TRAVEL_DISTANCE(PROPERTY_URN_PREFIX + "traveldistance"),
    WATER_QUANTITY(PROPERTY_URN_PREFIX + "waterquantity"),

    TEMPERATURE(PROPERTY_URN_PREFIX + "temperature"),
    IMPORTANCE(PROPERTY_URN_PREFIX + "importance");

    // CHECKSTYLE:ON:JavadocVariableCheck

    private String urn;

    private StandardPropertyURN(String urn) {
        this.urn = urn;
    }

    @Override
    public String toString() {
        return urn;
    }

    /**
       Convert a String to a StandardPropertyURN.
       @param s The String to convert.
       @return A StandardPropertyURN.
    */
    public static StandardPropertyURN fromString(String s) {
        for (StandardPropertyURN next : StandardPropertyURN.values()) {
            if (next.urn.equals(s)) {
                return next;
            }
        }
        throw new IllegalArgumentException(s);
    }
}
