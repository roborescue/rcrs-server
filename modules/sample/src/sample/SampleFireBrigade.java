package sample;

import static rescuecore2.misc.Handy.objectsToIDs;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.FireBrigade;

/**
   A sample fire brigade agent.
 */
public class SampleFireBrigade extends AbstractSampleAgent<FireBrigade> {
    private static final String MAX_WATER_KEY = "fire.tank.maximum";
    private static final String MAX_DISTANCE_KEY = "fire.extinguish.max-distance";
    private static final String MAX_POWER_KEY = "fire.extinguish.max-sum";

    private int maxWater;
    private int maxDistance;
    private int maxPower;

    @Override
    public String toString() {
        return "Sample fire brigade";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE,StandardEntityURN.HYDRANT,StandardEntityURN.GAS_STATION);
        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);
        Logger.info("Sample fire brigade connected: max extinguish distance = " + maxDistance + ", max power = " + maxPower + ", max tank = " + maxWater);
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
        FireBrigade me = me();
        // Are we currently filling with water?
        if (me.isWaterDefined() && me.getWater() < maxWater && location() instanceof Refuge) {
            Logger.info("Filling with water at " + location());
            sendRest(time);
            return;
        }
        // Are we out of water?
        if (me.isWaterDefined() && me.getWater() == 0) {
            // Head for a refuge
            List<EntityID> path = search.breadthFirstSearch(me().getPosition(), refugeIDs);
            if (path != null) {
                Logger.info("Moving to refuge");
                sendMove(time, path);
                return;
            }
            else {
                Logger.debug("Couldn't plan a path to a refuge.");
                path = randomWalk();
                Logger.info("Moving randomly");
                sendMove(time, path);
                return;
            }
        }
        // Find all buildings that are on fire
        Collection<EntityID> all = getBurningBuildings();
        // Can we extinguish any right now?
        for (EntityID next : all) {
            if (model.getDistance(getID(), next) <= maxDistance) {
                Logger.info("Extinguishing " + next);
                sendExtinguish(time, next, maxPower);
                sendSpeak(time, 1, ("Extinguishing " + next).getBytes());
                return;
            }
        }
        // Plan a path to a fire
        for (EntityID next : all) {
            List<EntityID> path = planPathToFire(next);
            if (path != null) {
                Logger.info("Moving to target");
                sendMove(time, path);
                return;
            }
        }
        List<EntityID> path = null;
        Logger.debug("Couldn't plan a path to a fire.");
        path = randomWalk();
        Logger.info("Moving randomly");
        sendMove(time, path);
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
    }

    private Collection<EntityID> getBurningBuildings() {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.BUILDING);
        List<Building> result = new ArrayList<Building>();
        for (StandardEntity next : e) {
            if (next instanceof Building) {
                Building b = (Building)next;
                if (b.isOnFire()) {
                    result.add(b);
                }
            }
        }
        // Sort by distance
        Collections.sort(result, new DistanceSorter(location(), model));
        return objectsToIDs(result);
    }

    private List<EntityID> planPathToFire(EntityID target) {
        // Try to get to anything within maxDistance of the target
        Collection<StandardEntity> targets = model.getObjectsInRange(target, maxDistance);
        if (targets.isEmpty()) {
            return null;
        }
        return search.breadthFirstSearch(me().getPosition(), objectsToIDs(targets));
    }
}
