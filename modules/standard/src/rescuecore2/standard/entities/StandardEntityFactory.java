package rescuecore2.standard.entities;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Enums;

import rescuecore2.registry.AbstractEntityFactory;
import rescuecore2.standard.entities.StandardEntityURN.StandardEntityURN_V1;
import rescuecore2.worldmodel.EntityID;

/**
 * EntityFactory that builds standard Robocup Standard objects.
 */
public final class StandardEntityFactory
		extends AbstractEntityFactory<StandardEntityURN> {

	/**
	 * Singleton class. Use this instance to do stuff.
	 */
	public static final StandardEntityFactory INSTANCE = new StandardEntityFactory();

	/**
	 * Singleton class: private constructor.
	 */
	private StandardEntityFactory() {
		super(StandardEntityURN.class);
	}

	@Override
	public StandardEntity makeEntity(StandardEntityURN urn, EntityID id) {
		switch (urn) {
		case WORLD:
			return new World(id);
		case ROAD:
			return new Road(id);
		case BUILDING:
			return new Building(id);
		case BLOCKADE:
			return new Blockade(id);
		case REFUGE:
			return new Refuge(id);
		case HYDRANT:
			return new Hydrant(id);
		case GAS_STATION:
			return new GasStation(id);
		case FIRE_STATION:
			return new FireStation(id);
		case AMBULANCE_CENTRE:
			return new AmbulanceCentre(id);
		case POLICE_OFFICE:
			return new PoliceOffice(id);
		case CIVILIAN:
			return new Civilian(id);
		case FIRE_BRIGADE:
			return new FireBrigade(id);
		case AMBULANCE_TEAM:
			return new AmbulanceTeam(id);
		case POLICE_FORCE:
			return new PoliceForce(id);
		default:
			throw new IllegalArgumentException(
					"Unrecognised entity urn: " + urn);
		}
	}

	@Override
	public String getV1Equiv(int urnId) {
		StandardEntityURN_V1 item = Enums
				.getIfPresent(StandardEntityURN_V1.class,
						StandardEntityURN.fromInt(urnId).name())
				.orNull();
		return item == null ? null : item.toString();
	}
}
