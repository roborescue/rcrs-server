package rescuecore2.standard.entities;

import static rescuecore2.standard.Constants.ENTITY_URN_PREFIX;
import static rescuecore2.standard.Constants.ENTITY_URN_PREFIX_V1;

import java.util.Map;

import rescuecore2.URN;

/**
 * URNs for standard entities.
 */
public enum StandardEntityURN implements URN {

	WORLD(ENTITY_URN_PREFIX | 1), ROAD(ENTITY_URN_PREFIX | 2),
	BLOCKADE(ENTITY_URN_PREFIX | 3), BUILDING(ENTITY_URN_PREFIX | 4),
	REFUGE(ENTITY_URN_PREFIX | 5), HYDRANT(ENTITY_URN_PREFIX | 6),
	GAS_STATION(ENTITY_URN_PREFIX | 7), FIRE_STATION(ENTITY_URN_PREFIX | 8),
	AMBULANCE_CENTRE(ENTITY_URN_PREFIX | 9),
	POLICE_OFFICE(ENTITY_URN_PREFIX | 10), CIVILIAN(ENTITY_URN_PREFIX | 11),
	FIRE_BRIGADE(ENTITY_URN_PREFIX | 12),
	AMBULANCE_TEAM(ENTITY_URN_PREFIX | 13),
	POLICE_FORCE(ENTITY_URN_PREFIX | 14);

	private int urn;
	private String urnString;
	public static final Map<Integer, StandardEntityURN> MAP = URN
			.generateMap(StandardEntityURN.class);
	public static final Map<String, StandardEntityURN> MAPSTR = URN
			.generateMapStr(StandardEntityURN.class);

	private StandardEntityURN(int urn) {// TODO remove
		this(urn, null);
	}

	private StandardEntityURN(int urn, String urnString) {
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

	public static StandardEntityURN fromInt(int urn) {
		return MAP.get(urn);
	}

	public static StandardEntityURN fromString(String urn) {
		return MAPSTR.get(urn);
	}

	/**
	 * URNs for standard entities.
	 */
	public enum StandardEntityURN_V1 {

		WORLD(ENTITY_URN_PREFIX_V1 + "world"),
		ROAD(ENTITY_URN_PREFIX_V1 + "road"),
		BLOCKADE(ENTITY_URN_PREFIX_V1 + "blockade"),
		BUILDING(ENTITY_URN_PREFIX_V1 + "building"),
		REFUGE(ENTITY_URN_PREFIX_V1 + "refuge"),
		HYDRANT(ENTITY_URN_PREFIX_V1 + "hydrant"),
		GAS_STATION(ENTITY_URN_PREFIX_V1 + "gasstation"),
		FIRE_STATION(ENTITY_URN_PREFIX_V1 + "firestation"),
		AMBULANCE_CENTRE(ENTITY_URN_PREFIX_V1 + "ambulancecentre"),
		POLICE_OFFICE(ENTITY_URN_PREFIX_V1 + "policeoffice"),
		CIVILIAN(ENTITY_URN_PREFIX_V1 + "civilian"),
		FIRE_BRIGADE(ENTITY_URN_PREFIX_V1 + "firebrigade"),
		AMBULANCE_TEAM(ENTITY_URN_PREFIX_V1 + "ambulanceteam"),
		POLICE_FORCE(ENTITY_URN_PREFIX_V1 + "policeforce");

		private String urn;

		private StandardEntityURN_V1(String urn) {
			this.urn = urn;
		}

		@Override
		public String toString() {
			return urn;
		}

		/**
		 * Convert a String to a StandardEntityURN.
		 *
		 * @param s The String to convert.
		 * @return A StandardEntityURN.
		 */
		public static StandardEntityURN_V1 fromString(String s) {
			for (StandardEntityURN_V1 next : StandardEntityURN_V1.values()) {
				if (next.urn.equals(s)) {
					return next;
				}
			}
			throw new IllegalArgumentException(s);
		}
	}
}