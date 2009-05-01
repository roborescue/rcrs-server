package rescuecore2.worldmodel.entities.legacy;

import java.util.HashMap;
import java.util.Map;

/**
   A bunch of useful constants for properties.
 */
public enum PropertyType {
    NULL("", 0),
	START_TIME("startTime", 1),
	LONGITUDE("longitude", 2),
	LATITUDE("latitude", 3),
	WIND_FORCE("windForce", 4),
	WIND_DIRECTION("windDirection", 5),

	HEAD("head", 6),
	TAIL("tail", 7),
	LENGTH("length", 8),

	ROAD_KIND("roadKind", 9),
	CARS_PASS_TO_HEAD("carsPassToHead", 10),
	CARS_PASS_TO_TAIL("carsPassToTail", 11),
	HUMANS_PASS_TO_HEAD("humansPassToHead", 12),
	HUMANS_PASS_TO_TAIL("humansPassToTail", 13),
	WIDTH("width", 14),
	BLOCK("block", 15),
	REPAIR_COST("repairCost", 16),
	MEDIAN_STRIP("hasMedianStrip", 17),
	LINES_TO_HEAD("linesToHead", 18),
	LINES_TO_TAIL("linesToTail", 19),
	WIDTH_FOR_WALKERS("widthForWalkers", 20),
	SIGNAL("hasSignal", 21),
	SHORTCUT_TO_TURN("shortcutToTurn", 22),
	POCKET_TO_TURN_ACROSS("pocketToTurnAcross", 23),
	SIGNAL_TIMING("signalTiming", 24),

	X("x", 25),
	Y("y", 26),
	EDGES("edges", 27),

	FLOORS("floors", 28),
	BUILDING_ATTRIBUTES("buildingAttributes", 29),
	IGNITION("ignition", 30),
	FIERYNESS("fieryness", 31),
	BROKENNESS("brokenness", 32),
	ENTRANCES("entrances", 33),
	BUILDING_CODE("buildingCode", 34),
	BUILDING_AREA_GROUND("groundArea", 35),
	BUILDING_AREA_TOTAL("totalArea", 36),
	BUILDING_APEXES("apexes", 37),

	POSITION("position", 38),
	POSITION_EXTRA("positionExtra", 39),
	DIRECTION("direction", 40),
	POSITION_HISTORY("positionHistory", 41),
	STAMINA("stamina", 42),
	HP("hp", 43),
	DAMAGE("damage", 44),
	BURIEDNESS("buriedness", 45),
	WATER_QUANTITY("water", 46),

	TEMPERATURE("temperature", 47),
	IMPORTANCE("importance", 48);

    public static PropertyType fromName(String name) {
	for (PropertyType next : PropertyType.values()) {
	    if (next.getName().equals(name)) return next;
	}
	throw new IllegalArgumentException("Unrecognised property name: " + name);
    }

    public static PropertyType fromID(int id) {
	for (PropertyType next : PropertyType.values()) {
	    if (next.getID() == id) return next;
	}
	throw new IllegalArgumentException("Unrecognised property id: " + id);
    }

    private int id;
    private String name;

    private PropertyType(String name, int id) {
	this.name = name;
	this.id = id;
    }

    public String getName() {
	return name;
    }

    public int getID() {
	return id;
    }
}