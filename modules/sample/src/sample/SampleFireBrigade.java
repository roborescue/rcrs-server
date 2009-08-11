package sample;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import rescuecore2.worldmodel.EntityID;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityType;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKExtinguish;

/**
   A sample fire brigade agent.
 */
public class SampleFireBrigade extends AbstractSampleAgent {
    private static final int MAX_WATER = 15000;
    private static final int EXTINGUISH_DISTANCE = 30000;
    private static final int EXTINGUISH_POWER = 1000;

    @Override
    public String toString() {
        return "Sample fire brigade";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        world.indexClass(StandardEntityType.BUILDING, StandardEntityType.REFUGE);
    }

    @Override
    protected void think(int time, List<EntityID> changed) {
        FireBrigade me = (FireBrigade)me();
        // Are we currently filling with water?
        if (me.getWater() != MAX_WATER && location() instanceof Refuge) {
            System.out.println(me() + " filling with water at " + location());
            return;
        }
        // Are we out of water?
        if (me.getWater() == 0) {
            // Head for a refuge
            List<EntityID> path = search.breadthFirstSearch(location(), getRefuges());
            if (path != null) {
                AKMove move = new AKMove(entityID, time, path);
                System.out.println(me() + " moving to refuge: " + move);
                send(move);
                return;
            }
            else {
                System.out.println(me() + " couldn't plan a path to a refuge.");
                send(new AKMove(entityID, time, randomWalk()));
            }
        }
        // Find all buildings that are on fire
        Collection<Building> all = getBurningBuildings();
        // Can we extinguish any right now?
        for (Building next : all) {
            if (world.getDistance(me, next) <= EXTINGUISH_DISTANCE) {
                AKExtinguish ex = new AKExtinguish(entityID, time, next.getID(), EXTINGUISH_POWER);
                System.out.println(me() + " extinguishing " + next + ": " + ex);
                send(ex);
                return;
            }
        }
        // Plan a path to a fire
        for (Building next : all) {
            List<EntityID> path = planPathToFire(next);
            if (path != null) {
                AKMove move = new AKMove(entityID, time, path);
                System.out.println(me() + " moving to fire: " + move);
                send(move);
                return;
            }
        }
        System.out.println(me() + " couldn't plan a path to a fire.");
        send(new AKMove(entityID, time, randomWalk()));
    }

    @Override
    public int[] getRequestedEntityIDs() {
        return new int[] {StandardEntityType.FIRE_BRIGADE.getID()
        };
    }

    private List<Refuge> getRefuges() {
        Collection<StandardEntity> e = world.getEntitiesOfType(StandardEntityType.REFUGE);
        List<Refuge> result = new ArrayList<Refuge>();
        for (StandardEntity next : e) {
            if (next instanceof Refuge) {
                result.add((Refuge)next);
            }
        }
        return result;
    }

    private List<Building> getBurningBuildings() {
        Collection<StandardEntity> e = world.getEntitiesOfType(StandardEntityType.BUILDING);
        List<Building> result = new ArrayList<Building>();
        for (StandardEntity next : e) {
            if (next instanceof Building) {
                Building b = (Building)next;
                // CHECKSTYLE:OFF:MagicNumber
                if (b.getFieryness() > 0 && b.getFieryness() < 4) {
                    // CHECKSTYLE:ON:MagicNumber
                    result.add(b);
                }
            }
        }
        // Sort by distance
        Collections.sort(result, new DistanceSorter(location(), world));
        return result;
    }

    private List<EntityID> planPathToFire(Building target) {
        // Try to get to anything within EXTINGUISH_DISTANCE of the target
        Collection<StandardEntity> targets = world.getObjectsInRange(target, EXTINGUISH_DISTANCE);
        if (targets.isEmpty()) {
            return null;
        }
        return search.breadthFirstSearch(location(), targets);
    }
}