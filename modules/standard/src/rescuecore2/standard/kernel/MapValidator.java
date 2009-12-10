package rescuecore2.standard.kernel;

import rescuecore2.worldmodel.EntityID;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Area;

import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A class for validating maps.
 */
public final class MapValidator {
    private static final Log LOG = LogFactory.getLog(MapValidator.class);

    private MapValidator() {}

    /**
       Validate a map.
       @param world The map to validate.
       @throws MapValidationException If the map is invalid.
     */
    public static void validate(StandardWorldModel world) throws MapValidationException {
        LOG.info("Validating map...");
        Set<StandardEntity> toVisit = new HashSet<StandardEntity>();
        LOG.debug("validating objects...");
        for (StandardEntity next : world) {
            if (next instanceof Building) {
                validateBuilding((Building)next, world);
                toVisit.add(next);
            }
            if (next instanceof Road) {
                validateRoad((Road)next, world);
                toVisit.add(next);
            }
        }
        LOG.debug("validating connectivity...");
        validateConnectivity(toVisit, world);
        LOG.info("done");
    }

    private static void validateArea(Area a, StandardWorldModel world) throws MapValidationException {
        // Check that all neighbours are areas
        for (EntityID next : a.getNeighbours()) {
            StandardEntity e = world.getEntity(next);
            if (!(e instanceof Area)) {
                throw new MapValidationException(a + " has non-area entity '" + e + "' as a neighbour.");
            }
            // Check that the neighbours knows about this building
            Area area = (Area)e;
            List<EntityID> areas = area.getNeighbours();
            if (!areas.contains(a.getID())) {
                throw new MapValidationException(a + " has " + area + " as a neighbour but that neighbour does not know about the building.");
            }
        }
    }

    private static void validateBuilding(Building b, StandardWorldModel world) throws MapValidationException {
        validateArea(b, world);
    }

    private static void validateRoad(Road r, StandardWorldModel world) throws MapValidationException {
        validateArea(r, world);
    }

    private static void validateConnectivity(Collection<StandardEntity> entities, StandardWorldModel world) throws MapValidationException {
        Set<StandardEntity> visited = new HashSet<StandardEntity>(entities.size());
        List<StandardEntity> open = new LinkedList<StandardEntity>();
        open.add(entities.iterator().next());
        while (!open.isEmpty()) {
            StandardEntity next = open.remove(0);
            if (visited.contains(next)) {
                continue;
            }
            visited.add(next);
            entities.remove(next);
            if (next instanceof Area) {
                for (EntityID neighbour : ((Area)next).getNeighbours()) {
                    open.add(world.getEntity(neighbour));
                }
            }
        }
        if (!entities.isEmpty()) {
            throw new MapValidationException("Connectivity test failed. Visited " + visited.size() + " entities; missed " + entities.size() + " entities.");
        }
        LOG.debug("connectivity ok (" + visited.size() + " entities)...");
    }

}