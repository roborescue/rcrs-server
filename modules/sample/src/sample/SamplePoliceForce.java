package sample;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Area;
import rescuecore2.misc.Pair;

/**
   A sample police force agent.
 */
public class SamplePoliceForce extends AbstractSampleAgent<PoliceForce> {
    @Override
    public String toString() {
        return "Sample police force";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.ROAD);
    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to channel 1
            sendSubscribe(time, 1);
        }
        for (Command next : heard) {
            Logger.debug("Heard " + next);
        }
        // Am I on (or next to) a blocked road?
        EntityID target = getTargetBlockade();
        if (target != null) {
            Logger.info("Clearing blockade " + target);
            sendSpeak(time, 1, ("Clearing " + target).getBytes());
            sendClear(time, target);
            return;
        }

        // Plan a path to a blocked area
        List<EntityID> path = search.breadthFirstSearch(location(), getBlockedRoads());
        if (path != null) {
            Logger.info("Moving to target");
            sendMove(time, path);
            return;
        }
        Logger.debug("Couldn't plan a path to a blocked road");
        Logger.info("Moving randomly");
        sendMove(time, randomWalk());
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }

    private List<Road> getBlockedRoads() {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.ROAD);
        List<Road> result = new ArrayList<Road>();
        for (StandardEntity next : e) {
            Road r = (Road)next;
            if (r.isBlockadesDefined() && !r.getBlockades().isEmpty()) {
                result.add(r);
            }
        }
        return result;
    }

    private EntityID getTargetBlockade() {
        Area location = (Area)location();
        EntityID result = getTargetBlockade(location);
        if (result != null) {
            return result;
        }
        for (EntityID next : location.getNeighbours()) {
            location = (Area)model.getEntity(next);
            result = getTargetBlockade(location);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private EntityID getTargetBlockade(Area area) {
        if (!area.isBlockadesDefined()) {
            return null;
        }
        List<EntityID> ids = area.getBlockades();
        if (ids.isEmpty()) {
            return null;
        }
        return ids.get(0);
    }

    /**
       Get the blockade that is nearest this agent.
       @return The EntityID of the nearest blockade, or null if there are no blockades in the agents current location.
    */
    public EntityID getNearestBlockade() {
        return getNearestBlockade((Area)location(), me().getX(), me().getY());
    }

    /**
       Get the blockade that is nearest a point.
       @param area The area to check.
       @param x The X coordinate to look up.
       @param y The X coordinate to look up.
       @return The EntityID of the nearest blockade, or null if there are no blockades in this area.
    */
    public EntityID getNearestBlockade(Area area, int x, int y) {
        double bestDistance = 0;
        EntityID best = null;
        Logger.debug("Finding nearest blockade");
        if (area.isBlockadesDefined()) {
            for (EntityID blockadeID : area.getBlockades()) {
                Logger.debug("Checking " + blockadeID);
                StandardEntity entity = model.getEntity(blockadeID);
                Logger.debug("Found " + entity);
                if (entity == null) {
                    continue;
                }
                Pair<Integer, Integer> location = entity.getLocation(model);
                Logger.debug("Location: " + location);
                if (location == null) {
                    continue;
                }
                double dx = location.first() - x;
                double dy = location.second() - y;
                double distance = Math.hypot(dx, dy);
                if (best == null || distance < bestDistance) {
                    bestDistance = distance;
                    best = entity.getID();
                }
            }
        }
        Logger.debug("Nearest blockade: " + best);
        return best;
    }
}