package rescuecore2.standard.messages;

import static rescuecore2.standard.Constants.MESSAGE_URN_PREFIX;
import static rescuecore2.standard.Constants.MESSAGE_URN_PREFIX_V1;

import java.util.Map;

import rescuecore2.URN;

/**
 * URNs for standard messages.
 */
public enum StandardMessageURN implements URN {

	/** Rest command. */
	AK_REST(MESSAGE_URN_PREFIX | 1),
	/** Move command. */
	AK_MOVE(MESSAGE_URN_PREFIX | 2),
	/** Load command. */
	AK_LOAD(MESSAGE_URN_PREFIX | 3),
	/** Unload command. */
	AK_UNLOAD(MESSAGE_URN_PREFIX | 4),
	/** Say command. */
	AK_SAY(MESSAGE_URN_PREFIX | 5),
	/** Tell command. */
	AK_TELL(MESSAGE_URN_PREFIX | 6),
	/** Extinguish command. */
	AK_EXTINGUISH(MESSAGE_URN_PREFIX | 7),
	/** Rescue command. */
	AK_RESCUE(MESSAGE_URN_PREFIX | 8),
	/** Clear command. */
	AK_CLEAR(MESSAGE_URN_PREFIX | 9),
	/** Clear-Area command. */
	AK_CLEAR_AREA(MESSAGE_URN_PREFIX | 10),

	/** Channel subscribe command. */
	AK_SUBSCRIBE(MESSAGE_URN_PREFIX | 11),

	/** Channel speak command. */
	AK_SPEAK(MESSAGE_URN_PREFIX | 12);

	private int urn;
	private String urnString;
	public static final Map<Integer, StandardMessageURN> MAP = URN
			.generateMap(StandardMessageURN.class);
	public static final Map<String, StandardMessageURN> MAPSTR = URN
			.generateMapStr(StandardMessageURN.class);

	private StandardMessageURN(int urn) {// TODO remove
		this(urn, null);
	}

	private StandardMessageURN(int urn, String urnString) {
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
	 * Convert a String to a StandardMessageURN.
	 *
	 * @param s The String to convert.
	 * @return A StandardMessageURN.
	 */
	public static StandardMessageURN fromInt(int s) {
		return MAP.get(s);
	}

	public static StandardMessageURN fromString(int s) {
		return MAP.get(s);
	}

	/**
	 * URNs for standard messages.
	 */
	public enum StandardMessageURN_V1 {
		/** Rest command. */
		AK_REST(MESSAGE_URN_PREFIX_V1 + "rest"),
		/** Move command. */
		AK_MOVE(MESSAGE_URN_PREFIX_V1 + "move"),
		/** Load command. */
		AK_LOAD(MESSAGE_URN_PREFIX_V1 + "load"),
		/** Unload command. */
		AK_UNLOAD(MESSAGE_URN_PREFIX_V1 + "unload"),
		/** Say command. */
		AK_SAY(MESSAGE_URN_PREFIX_V1 + "say"),
		/** Tell command. */
		AK_TELL(MESSAGE_URN_PREFIX_V1 + "tell"),
		/** Extinguish command. */
		AK_EXTINGUISH(MESSAGE_URN_PREFIX_V1 + "extinguish"),
		/** Rescue command. */
		AK_RESCUE(MESSAGE_URN_PREFIX_V1 + "rescue"),
		/** Clear command. */
		AK_CLEAR(MESSAGE_URN_PREFIX_V1 + "clear"),
		/** Clear-Area command. */
		AK_CLEAR_AREA(MESSAGE_URN_PREFIX_V1 + "clear_area"),

		/** Channel subscribe command. */
		AK_SUBSCRIBE(MESSAGE_URN_PREFIX_V1 + "subscribe"),

		/** Channel speak command. */
		AK_SPEAK(MESSAGE_URN_PREFIX_V1 + "speak");

		private String urn;

		private StandardMessageURN_V1(String urn) {
			this.urn = urn;
		}

		@Override
		public String toString() {
			return urn;
		}

		/**
		 * Convert a String to a StandardMessageURN.
		 *
		 * @param s The String to convert.
		 * @return A StandardMessageURN.
		 */
		public static StandardMessageURN_V1 fromString(String s) {
			for (StandardMessageURN_V1 next : StandardMessageURN_V1.values()) {
				if (next.urn.equals(s)) {
					return next;
				}
			}
			throw new IllegalArgumentException(s);
		}
	}
}