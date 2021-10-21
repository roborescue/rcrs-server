package rescuecore2.standard.entities;

import static rescuecore2.standard.Constants.PROPERTY_URN_PREFIX;
import static rescuecore2.standard.Constants.PROPERTY_URN_PREFIX_V1;

import java.util.Map;

import rescuecore2.URN;;

/**
 * URNs for standard property types.
 */
public enum StandardPropertyURN implements URN {

	START_TIME(PROPERTY_URN_PREFIX | 1), LONGITUDE(PROPERTY_URN_PREFIX | 2),
	LATITUDE(PROPERTY_URN_PREFIX | 3), WIND_FORCE(PROPERTY_URN_PREFIX | 4),
	WIND_DIRECTION(PROPERTY_URN_PREFIX | 5),

	X(PROPERTY_URN_PREFIX | 6), Y(PROPERTY_URN_PREFIX | 7),

	BLOCKADES(PROPERTY_URN_PREFIX | 8), REPAIR_COST(PROPERTY_URN_PREFIX | 9),

	FLOORS(PROPERTY_URN_PREFIX | 10),
	BUILDING_ATTRIBUTES(PROPERTY_URN_PREFIX | 11),
	IGNITION(PROPERTY_URN_PREFIX | 12), FIERYNESS(PROPERTY_URN_PREFIX | 13),
	BROKENNESS(PROPERTY_URN_PREFIX | 14),
	BUILDING_CODE(PROPERTY_URN_PREFIX | 15),
	BUILDING_AREA_GROUND(PROPERTY_URN_PREFIX | 16),
	BUILDING_AREA_TOTAL(PROPERTY_URN_PREFIX | 17),
	APEXES(PROPERTY_URN_PREFIX | 18), EDGES(PROPERTY_URN_PREFIX | 19),

	POSITION(PROPERTY_URN_PREFIX | 20), DIRECTION(PROPERTY_URN_PREFIX | 21),
	POSITION_HISTORY(PROPERTY_URN_PREFIX | 22),
	STAMINA(PROPERTY_URN_PREFIX | 23), HP(PROPERTY_URN_PREFIX | 24),
	DAMAGE(PROPERTY_URN_PREFIX | 25), BURIEDNESS(PROPERTY_URN_PREFIX | 26),
	TRAVEL_DISTANCE(PROPERTY_URN_PREFIX | 27),
	WATER_QUANTITY(PROPERTY_URN_PREFIX | 28),

	TEMPERATURE(PROPERTY_URN_PREFIX | 29), IMPORTANCE(PROPERTY_URN_PREFIX | 30),
	CAPACITY(PROPERTY_URN_PREFIX | 31), BEDCAPACITY(PROPERTY_URN_PREFIX | 32),
	OCCUPIEDBEDS(PROPERTY_URN_PREFIX | 33),
	REFILLCAPACITY(PROPERTY_URN_PREFIX | 34),
	WAITINGLISTSIZE(PROPERTY_URN_PREFIX | 35);

	private int urn;
	private String urnString;
	public static final Map<Integer, StandardPropertyURN> MAP = URN
			.generateMap(StandardPropertyURN.class);
	public static final Map<String, StandardPropertyURN> MAPSTR = URN
			.generateMapStr(StandardPropertyURN.class);

	private StandardPropertyURN(int urn) {// TODO remove
		this(urn, null);
	}

	private StandardPropertyURN(int urn, String urnString) {
		this.urn = urn;
		this.urnString = urnString;
	}

	@Override
	public int getUrn() {
		return urn;
	}

	@Override
	public String getUrnString() {
		return urnString;
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

	/**
	 * URNs for standard property types.
	 */
	public enum StandardPropertyURN_V1 {

		START_TIME(PROPERTY_URN_PREFIX_V1 + "starttime"),
		LONGITUDE(PROPERTY_URN_PREFIX_V1 + "longitude"),
		LATITUDE(PROPERTY_URN_PREFIX_V1 + "latitude"),
		WIND_FORCE(PROPERTY_URN_PREFIX_V1 + "windforce"),
		WIND_DIRECTION(PROPERTY_URN_PREFIX_V1 + "winddirection"),

		X(PROPERTY_URN_PREFIX_V1 + "x"), Y(PROPERTY_URN_PREFIX_V1 + "y"),

		BLOCKADES(PROPERTY_URN_PREFIX_V1 + "blockades"),
		REPAIR_COST(PROPERTY_URN_PREFIX_V1 + "repaircost"),

		FLOORS(PROPERTY_URN_PREFIX_V1 + "floors"),
		BUILDING_ATTRIBUTES(PROPERTY_URN_PREFIX_V1 + "buildingattributes"),
		IGNITION(PROPERTY_URN_PREFIX_V1 + "ignition"),
		FIERYNESS(PROPERTY_URN_PREFIX_V1 + "fieryness"),
		BROKENNESS(PROPERTY_URN_PREFIX_V1 + "brokenness"),
		BUILDING_CODE(PROPERTY_URN_PREFIX_V1 + "buildingcode"),
		BUILDING_AREA_GROUND(PROPERTY_URN_PREFIX_V1 + "buildingareaground"),
		BUILDING_AREA_TOTAL(PROPERTY_URN_PREFIX_V1 + "buildingareatotal"),
		APEXES(PROPERTY_URN_PREFIX_V1 + "apexes"),
		EDGES(PROPERTY_URN_PREFIX_V1 + "edges"),

		POSITION(PROPERTY_URN_PREFIX_V1 + "position"),
		DIRECTION(PROPERTY_URN_PREFIX_V1 + "direction"),
		POSITION_HISTORY(PROPERTY_URN_PREFIX_V1 + "positionhistory"),
		STAMINA(PROPERTY_URN_PREFIX_V1 + "stamina"),
		HP(PROPERTY_URN_PREFIX_V1 + "hp"),
		DAMAGE(PROPERTY_URN_PREFIX_V1 + "damage"),
		BURIEDNESS(PROPERTY_URN_PREFIX_V1 + "buriedness"),
		TRAVEL_DISTANCE(PROPERTY_URN_PREFIX_V1 + "traveldistance"),
		WATER_QUANTITY(PROPERTY_URN_PREFIX_V1 + "waterquantity"),

		TEMPERATURE(PROPERTY_URN_PREFIX_V1 + "temperature"),
		IMPORTANCE(PROPERTY_URN_PREFIX_V1 + "importance"),
		CAPACITY(PROPERTY_URN_PREFIX_V1 + "capacity"),
		BEDCAPACITY(PROPERTY_URN_PREFIX_V1 + "bedCapacity"),
		OCCUPIEDBEDS(PROPERTY_URN_PREFIX_V1 + "occupiedBeds"),
		REFILLCAPACITY(PROPERTY_URN_PREFIX_V1 + "refillCapacity"),
		WAITINGLISTSIZE(PROPERTY_URN_PREFIX_V1 + "waitingListSize");

		private String urn;

		private StandardPropertyURN_V1(String urn) {
			this.urn = urn;
		}

		@Override
		public String toString() {
			return urn;
		}

		/**
		 * Convert a String to a StandardPropertyURN.
		 *
		 * @param s The String to convert.
		 * @return A StandardPropertyURN.
		 */
		public static StandardPropertyURN_V1 fromString(String s) {
			for (StandardPropertyURN_V1 next : StandardPropertyURN_V1
					.values()) {
				if (next.urn.equals(s)) {
					return next;
				}
			}
			throw new IllegalArgumentException(s);
		}
	}

}
