package blockade;

import rescuecore2.messages.control.KSCommands;
import rescuecore2.messages.control.KSUpdate;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.misc.Pair;

import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Node;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A simple blockade simulator.
 */
public class BlockadeSimulator extends StandardSimulator {
    private static final Log LOG = LogFactory.getLog(BlockadeSimulator.class);

    private static final int RUBBLE_DIVISOR = 2000;
    private static final double NEARBY_THRESHOLD = 5000;

    private ChangeSet changes;
    private Map<EntityID, Set<Road>> nearbyRoads;

    @Override
    public String getName() {
        return "Basic blockade simulator";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        nearbyRoads = new LazyMap<EntityID, Set<Road>>() {
            public Set<Road> createValue() {
                return new HashSet<Road>();
            }
        };
        LOG.debug("Computing roads near buildings...");
        for (Entity next : model) {
            if (next instanceof Building) {
                Building b = (Building)next;
                for (Entity road : model) {
                    if (road instanceof Road) {
                        Road r = (Road)road;
                        if (isNear(r, b)) {
                            nearbyRoads.get(b.getID()).add(r);
                        }
                    }
                }
            }
        }
        LOG.debug("Done");
        changes = new ChangeSet();
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet cs) {
        cs.merge(changes);
    }

    @Override
    protected void handleUpdate(KSUpdate u) {
        super.handleUpdate(u);
        changes = new ChangeSet();
        for (EntityID id : u.getChangeSet().getChangedEntities()) {
            Entity next = model.getEntity(id);
            if (next instanceof Building) {
                Building b = (Building)next;
                Property brokenness = u.getChangeSet().getChangedProperty(id, StandardPropertyURN.BROKENNESS.name());
                if (brokenness != null) {
                    // Brokenness has changed. Add some blockedness to nearby roads
                    LOG.debug(b + " is broken. Updating nearby roads");
                    for (Road r : nearbyRoads.get(b.getID())) {
                        int width = r.isWidthDefined() ? r.getWidth() : 0;
                        int block = r.isBlockDefined() ? r.getBlock() : 0;
                        int increase = calculateBlock(b);
                        LOG.debug("Increasing block of " + r + " by " + increase);
                        block += increase;
                        if (block > width) {
                            block = width;
                        }
                        LOG.debug("New block: " + block);
                        r.setBlock(block);
                        changes.addChange(r, r.getBlockProperty());
                    }
                }
            }
        }
    }

    private int calculateBlock(Building b) {
        if (!b.isBrokennessDefined() || !b.isGroundAreaDefined() || !b.isFloorsDefined()) {
            return 0;
        }
        long rubble = b.getBrokenness() * b.getGroundArea() * b.getFloors();
        return (int)(rubble / RUBBLE_DIVISOR);
    }

    private boolean isNear(Road r, Building b) {
        return isNear((Node)r.getHead(model), b) || isNear((Node)r.getTail(model), b);
    }

    private boolean isNear(Node n, Building b) {
        if (n == null) {
            return false;
        }
        if (b.getEntrances().contains(n.getID())) {
            return true;
        }
        Pair<Integer, Integer> node = n.getLocation(model);
        if (b.isApexesDefined()) {
            for (Pair<Integer, Integer> apex : b.getApexesAsList()) {
                double d = Math.hypot(apex.first() - node.first(), apex.second() - node.second());
                if (d < NEARBY_THRESHOLD) {
                    return true;
                }
            }
        }
        if (b.isXDefined() && b.isYDefined()) {
            double d = Math.hypot(b.getX() - node.first(), b.getY() - node.second());
            if (d < NEARBY_THRESHOLD) {
                return true;
            }
        }
        return false;
    }
}