package rescuecore2.version0.entities;

import rescuecore2.worldmodel.PropertyType;

/**
   All types of property available in version0.
 */
public enum RescuePropertyType implements PropertyType {
    // CHECKSTYLE:OFF:JavadocVariableChec

    START_TIME(1, "startTime"),
    LONGITUDE(2, "longitude"),
    LATITUDE(3, "latitude"),
    WIND_FORCE(4, "windForce"),
    WIND_DIRECTION(5, "windDirection"),

    HEAD(6, "head"),
    TAIL(7, "tail"),
    LENGTH(8, "length"),

    ROAD_KIND(9, "roadKind"),
    CARS_PASS_TO_HEAD(10, "carsPassToHead"),
    CARS_PASS_TO_TAIL(11, "carsPassToTail"),
    HUMANS_PASS_TO_HEAD(12, "humansPassToHead"),
    HUMANS_PASS_TO_TAIL(13, "humansPassToTail"),
    WIDTH(14, "width"),
    BLOCK(15, "block"),
    REPAIR_COST(16, "repairCost"),
    MEDIAN_STRIP(17, "hasMedianStrip"),
    LINES_TO_HEAD(18, "linesToHead"),
    LINES_TO_TAIL(19, "linesToTail"),
    WIDTH_FOR_WALKERS(20, "widthForWalkers"),
    SIGNAL(21, "hasSignal"),
    SHORTCUT_TO_TURN(22, "shortcutToTurn"),
    POCKET_TO_TURN_ACROSS(23, "pocketToTurnAcross"),
    SIGNAL_TIMING(24, "signalTiming"),

    X(25, "x"),
    Y(26, "y"),
    EDGES(27, "edges"),

    FLOORS(28, "floors"),
    BUILDING_ATTRIBUTES(29, "buildingAttributes"),
    IGNITION(30, "ignition"),
    FIERYNESS(31, "fieryness"),
    BROKENNESS(32, "brokenness"),
    ENTRANCES(33, "entrances"),
    BUILDING_CODE(34, "buildingCode"),
    BUILDING_AREA_GROUND(35, "groundArea"),
    BUILDING_AREA_TOTAL(36, "totalArea"),
    BUILDING_APEXES(37, "apexes"),

    POSITION(38, "position"),
    POSITION_EXTRA(39, "positionExtra"),
    DIRECTION(40, "direction"),
    POSITION_HISTORY(41, "positionHistory"),
    STAMINA(42, "stamina"),
    HP(43, "hp"),
    DAMAGE(44, "damage"),
    BURIEDNESS(45, "buriedness"),
    WATER_QUANTITY(46, "water"),

    TEMPERATURE(47, "temperature"),
    IMPORTANCE(48, "importance");

    // CHECKSTYLE:ON:JavadocVariableCheck

    private int id;
    private String name;

    private RescuePropertyType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}