package kernel.standard;

import rescuecore2.components.AbstractAgent;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityType;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.messages.AKExtinguish;
import rescuecore2.standard.messages.AKMove;

import sample.SampleSearch;

import java.util.List;
import java.util.Collection;

/**
   A basic fire brigade agent that will try to extinguish a given target. If the target is a refuge then the fire brigade will attempt to enter the building to replenish water. If there is no target then this agent does nothing.
 */
public class ControlledFireBrigade extends AbstractAgent {
    private static final int MAX_WATER = 15000;
    private static final int EXTINGUISH_DISTANCE = 30000;
    private static final int EXTINGUISH_POWER = 1000;

    private StandardWorldModel world;
    private SampleSearch search;
    private Building target;

    /**
       Set the target of this fire brigade.
       @param target The new target.
     */
    public void setTarget(Building target) {
        this.target = target;
    }

    @Override
    protected void think(int time, List changed) {
        if (target == null) {
            System.out.println(me() + " has nothing to do");
            return;
        }
        if (target instanceof Refuge) {
            // Just go there
            List<EntityID> path = search.breadthFirstSearch(location(), target);
            if (path != null) {
                AKMove move = new AKMove(entityID, time, path);
                System.out.println(me() + " moving to refuge: " + move);
                send(move);
                return;
            }
            else {
                System.out.println(me() + " couldn't plan a path to refuge.");
            }
        }
        // Are we close enough to extinguish?
        int distance = world.getDistance((StandardEntity)me(), target);
        if (distance < EXTINGUISH_DISTANCE) {
            AKExtinguish ex = new AKExtinguish(entityID, time, target.getID(), EXTINGUISH_POWER);
            System.out.println(me() + " extinguishing " + target + ": " + ex);
            send(ex);
            return;
        }
        // Otherwise plan a path
        if (!target.equals(location())) {
            List<EntityID> path = planPathToFire();
            if (path != null) {
                AKMove move = new AKMove(entityID, time, path);
                System.out.println(me() + " moving to target: " + move);
                send(move);
                return;
            }
            else {
                System.out.println(me() + " couldn't plan a path to target.");
            }
        }
    }

    private List<EntityID> planPathToFire() {
        // Try to get to anything within EXTINGUISH_DISTANCE of the target
        Collection<StandardEntity> targets = world.getObjectsInRange(target, EXTINGUISH_DISTANCE);
        if (targets.isEmpty()) {
            return null;
        }
        return search.breadthFirstSearch(location(), targets);
    }

    @Override
    public int[] getRequestedEntityIDs() {
        return new int[] {StandardEntityType.FIRE_BRIGADE.getID()};
    }

    @Override
    protected WorldModel<StandardEntity> createWorldModel() {
        world = new StandardWorldModel();
        return world;
    }

    /**
       Get the location of the entity controlled by this agent.
       @return The location of the entity controlled by this agent.
    */
    protected StandardEntity location() {
        Human me = (Human)me();
        return me.getPosition(world);
    }

    @Override
    protected void postConnect() {
        world.index();
        search = new SampleSearch(world, true);
    }

    @Override
    public String toString() {
        return "Fire brigade " + me().getID() + " (" + ((FireBrigade)me()).getWater() + " water)" + (target == null ? " (no target)" : " target: building " + target.getID());
    }
}

