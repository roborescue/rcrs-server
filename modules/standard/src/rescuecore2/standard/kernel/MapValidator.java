package rescuecore2.standard.kernel;

import rescuecore2.worldmodel.EntityID;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Node;
import rescuecore2.standard.entities.Building;

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
            if (next instanceof Node) {
                validateNode((Node)next, world);
                toVisit.add(next);
            }
        }
        LOG.debug("validating connectivity...");
        validateConnectivity(toVisit, world);
        LOG.info("done");
    }

    private static void validateBuilding(Building b, StandardWorldModel world) throws MapValidationException {
        List<EntityID> entrances = b.getEntrances();
        for (EntityID next : entrances) {
            // Check that the entrance is a node
            StandardEntity e = world.getEntity(next);
            if (!(e instanceof Node)) {
                throw new MapValidationException(b + " has non-node entity '" + e + "' as an entrance.");
            }
            // Check that the node knows about the building
            Node n = (Node)e;
            List<EntityID> edges = n.getEdges();
            if (!edges.contains(b.getID())) {
                throw new MapValidationException(b + " has " + e + " as an entrance but the node does not have the building as an edge.");
            }
        }
    }

    private static void validateRoad(Road r, StandardWorldModel world) throws MapValidationException {
        StandardEntity head = r.getHead(world);
        StandardEntity tail = r.getTail(world);
        if (!(head instanceof Node)) {
            throw new MapValidationException(r + " has non-node entity '" + head + "' as head.");
        }
        if (!(tail instanceof Node)) {
            throw new MapValidationException(r + " has non-node entity '" + tail + "' as tail.");
        }
        // Check that the head and tail know about the road
        Node n = (Node)head;
        List<EntityID> edges = n.getEdges();
        if (!edges.contains(r.getID())) {
            throw new MapValidationException(r + " has " + n + " as a head but the node does not have the road as an edge.");
        }
        n = (Node)tail;
        edges = n.getEdges();
        if (!edges.contains(r.getID())) {
            throw new MapValidationException(r + " has " + n + " as a tail but the node does not have the road as an edge.");
        }
        if (r.getLinesToHead() < 1) {
            throw new MapValidationException(r + " has " + r.getLinesToHead() + " lines to head.");
        }
        if (r.getLinesToTail() < 1) {
            throw new MapValidationException(r + " has " + r.getLinesToTail() + " lines to tail.");
        }
        if (r.getLinesToHead() != r.getLinesToTail()) {
            throw new MapValidationException(r + " has " + r.getLinesToHead() + " lines to head and " + r.getLinesToTail() + " lines to tail.");
        }
    }

    private static void validateNode(Node n, StandardWorldModel world) throws MapValidationException {
        // Check that all edges know about the node
        EntityID nodeID = n.getID();
        for (EntityID next : n.getEdges()) {
            StandardEntity e = world.getEntity(next);
            if (e instanceof Road) {
                EntityID head = ((Road)e).getHead();
                EntityID tail = ((Road)e).getTail();
                if (!nodeID.equals(head) && !nodeID.equals(tail)) {
                    throw new MapValidationException(n + " has " + e + " as an edge but is neither the head nor tail of that road.");
                }
            }
            else if (e instanceof Building) {
                if (!((Building)e).getEntrances().contains(nodeID)) {
                    throw new MapValidationException(n + " has " + e + " as an edge but is not an entrance to that building.");
                }
            }
            else {
                throw new MapValidationException(n + " has " + e + " as an edge but it is not a road or a building.");
            }
        }
        // Check that shortcut/timing/pocketToTurn are the right lengths
        int count = n.getEdges().size();
        if (n.getShortcutToTurn().length != count) {
            throw new MapValidationException(n + " has " + count + " edges but shortcutToTurn has " + n.getShortcutToTurn().length + " entries. It should have " + count + ".");
        }
        if (n.getPocketToTurnAcross().length != (count * 2)) {
            throw new MapValidationException(n + " has " + count + " edges but pocketToTurnAcross has " + n.getPocketToTurnAcross().length + " entries. It should have " + (count * 2) + ".");
        }
        // CHECKSTYLE:OFF:MagicNumber
        if (n.getSignalTiming().length != (count * 3)) {
            throw new MapValidationException(n + " has " + count + " edges but signalTiming has " + n.getSignalTiming().length + " entries. It should have " + (count * 3) + ".");
        }
        // CHECKSTYLE:ON:MagicNumber
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
            if (next instanceof Road) {
                open.add(((Road)next).getHead(world));
                open.add(((Road)next).getTail(world));
            }
            if (next instanceof Building) {
                for (EntityID entrance : ((Building)next).getEntrances()) {
                    open.add(world.getEntity(entrance));
                }
            }
            if (next instanceof Node) {
                for (EntityID edge : ((Node)next).getEdges()) {
                    open.add(world.getEntity(edge));
                }
            }
        }
        if (!entities.isEmpty()) {
            throw new MapValidationException("Connectivity test failed. Visited " + visited.size() + " entities; missed " + entities.size() + " entities.");
        }
        LOG.debug("connectivity ok (" + visited.size() + " entities)...");
    }

}