package sample;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import rescuecore2.worldmodel.EntityID;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKExtinguish;

/**
   A sample fire brigade agent.
 */
public class SampleFireBrigade extends AbstractSampleAgent {
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
        world.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);
        System.out.println("Sample fire brigade connected: max extinguish distance = " + maxDistance + ", max power = " + maxPower + ", max tank = " + maxWater);
    }

    @Override
    protected void think(int time, Collection<EntityID> changed) {
        FireBrigade me = (FireBrigade)me();
        // Are we currently filling with water?
        if (me.getWater() < maxWater && location() instanceof Refuge) {
            //            System.out.println(me() + " filling with water at " + location());
            return;
        }
        // Are we out of water?
        if (me.isWaterDefined() && me.getWater() == 0) {
            // Head for a refuge
            List<EntityID> path = search.breadthFirstSearch(location(), getRefuges());
            if (path != null) {
                AKMove move = new AKMove(getID(), time, path);
                //                System.out.println(me() + " moving to refuge: " + move);
                send(move);
                return;
            }
            else {
                //                System.out.println(me() + " couldn't plan a path to a refuge.");
                send(new AKMove(getID(), time, randomWalk()));
            }
        }
        // Find all buildings that are on fire
        Collection<Building> all = getBurningBuildings();
        // Can we extinguish any right now?
        for (Building next : all) {
            if (world.getDistance(me, next) <= maxDistance) {
                AKExtinguish ex = new AKExtinguish(getID(), time, next.getID(), maxPower);
                //                System.out.println(me() + " extinguishing " + next + ": " + ex);
                send(ex);
                return;
            }
        }
        // Plan a path to a fire
        for (Building next : all) {
            List<EntityID> path = planPathToFire(next);
            if (path != null) {
                AKMove move = new AKMove(getID(), time, path);
                //                System.out.println(me() + " moving to fire: " + move);
                send(move);
                return;
            }
        }
        //        System.out.println(me() + " couldn't plan a path to a fire.");
        send(new AKMove(getID(), time, randomWalk()));
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
    }

    private List<Refuge> getRefuges() {
        Collection<StandardEntity> e = world.getEntitiesOfType(StandardEntityURN.REFUGE);
        List<Refuge> result = new ArrayList<Refuge>();
        for (StandardEntity next : e) {
            if (next instanceof Refuge) {
                result.add((Refuge)next);
            }
        }
        return result;
    }

    private List<Building> getBurningBuildings() {
        Collection<StandardEntity> e = world.getEntitiesOfType(StandardEntityURN.BUILDING);
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
        Collections.sort(result, new DistanceSorter(location(), world));
        return result;
    }

    private List<EntityID> planPathToFire(Building target) {
        // Try to get to anything within maxDistance of the target
        Collection<StandardEntity> targets = world.getObjectsInRange(target, maxDistance);
        if (targets.isEmpty()) {
            return null;
        }
        return search.breadthFirstSearch(location(), targets);
    }
}