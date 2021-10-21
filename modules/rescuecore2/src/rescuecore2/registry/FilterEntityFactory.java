package rescuecore2.registry;

import java.util.Set;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
 * A entity factory that filters urns that match or don't match a given set.
 */
public class FilterEntityFactory extends AbstractFilterFactory<EntityFactory>
		implements EntityFactory {

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
		super(downstream, urns, inclusive);
	}

	@Override
	public Entity makeEntity(int urn, EntityID id) {
		if (!isValidUrn(urn))
			return null;
		return downstream.makeEntity(urn, id);
	}

}
