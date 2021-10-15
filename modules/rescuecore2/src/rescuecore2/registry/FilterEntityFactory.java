package rescuecore2.registry;

import java.util.Map;
import java.util.Set;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
 * A entity factory that filters urns that match or don't match a given set.
 */
public class FilterEntityFactory implements EntityFactory {
	private EntityFactory downstream;
	private Set<Integer> urns;
	private boolean inclusive;

	/**
	 * Construct a FilterEntityFactory.
	 * 
	 * @param downstream The downstream entity factory.
	 * @param urns       The set of URNs.
	 * @param inclusive  True if the set of URNs are allowed, false if they are
	 *                   forbidden.
	 */
	public FilterEntityFactory(EntityFactory downstream, Set<Integer> urns,
			boolean inclusive) {
		this.downstream = downstream;
		this.urns = urns;
		this.inclusive = inclusive;
	}

	@Override
	public int[] getKnownEntityURNs() {
		return downstream.getKnownEntityURNs();
	}

	@Override
	public Entity makeEntity(int urn, EntityID id) {
		if (inclusive && !urns.contains(urn)) {
			return null;
		}
		if (!inclusive && urns.contains(urn)) {
			return null;
		}
		return downstream.makeEntity(urn, id);
	}

	@Override
	public String getV1Equiv(int urnId) {
		return downstream.getV1Equiv(urnId);
	}

	@Override
	public String getPrettyName(int urn) {
		return downstream.getPrettyName(urn);
	}
}
