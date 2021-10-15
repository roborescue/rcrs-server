package rescuecore2.standard.entities;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Enums;

import rescuecore2.registry.AbstractPropertyFactory;
import rescuecore2.standard.entities.StandardPropertyURN.StandardPropertyURN_V1;
import rescuecore2.standard.messages.StandardMessageURN;
import rescuecore2.standard.messages.StandardMessageURN.StandardMessageURN_V1;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.BooleanProperty;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;

/**
 * PropertyFactory that builds standard Robocup Standard properties.
 */
public final class StandardPropertyFactory
		extends AbstractPropertyFactory<StandardPropertyURN> {

	/**
	 * Singleton class. Use this instance to do stuff.
	 */
	public static final StandardPropertyFactory INSTANCE = new StandardPropertyFactory();

	/**
	 * Singleton class: private constructor.
	 */
	private StandardPropertyFactory() {
		super(StandardPropertyURN.class);
	}

	@Override
	public Property makeProperty(StandardPropertyURN urn) {
		switch (urn) {
		case START_TIME:
		case LONGITUDE:
		case LATITUDE:
		case WIND_FORCE:
		case WIND_DIRECTION:
		case X:
		case Y:
		case FLOORS:
		case BUILDING_ATTRIBUTES:
		case FIERYNESS:
		case BROKENNESS:
		case BUILDING_CODE:
		case BUILDING_AREA_GROUND:
		case BUILDING_AREA_TOTAL:
		case DIRECTION:
		case STAMINA:
		case HP:
		case DAMAGE:
		case BURIEDNESS:
		case WATER_QUANTITY:
		case TEMPERATURE:
		case IMPORTANCE:
		case TRAVEL_DISTANCE:
		case REPAIR_COST:
		case CAPACITY:
		case BEDCAPACITY:
		case REFILLCAPACITY:
		case OCCUPIEDBEDS:
		case WAITINGLISTSIZE:
			return new IntProperty(urn);
		case APEXES:
		case POSITION_HISTORY:
			return new IntArrayProperty(urn);
		case IGNITION:
			return new BooleanProperty(urn);
		case POSITION:
			return new EntityRefProperty(urn);
		case BLOCKADES:
			return new EntityRefListProperty(urn);
		case EDGES:
			return new EdgeListProperty(urn);
		default:
			throw new IllegalArgumentException(
					"Unrecognised property urn: " + urn);
		}
	}

	@Override
	public String getV1Equiv(int urnId) {
		StandardPropertyURN_V1 item = Enums
				.getIfPresent(StandardPropertyURN_V1.class,
						StandardPropertyURN.fromInt(urnId).name())
				.orNull();
		return item == null ? null : item.toString();
	}
}
