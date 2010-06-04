package human;

import static rescuecore2.misc.Handy.objectsToIDs;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.components.StandardAgent;

import sample.SampleSearch;

import java.util.List;
import java.util.Collection;
import java.util.EnumSet;

/**
   A basic fire brigade agent that will try to extinguish a given target. If the target is a refuge then the fire brigade will attempt to enter the building to replenish water. If there is no target then this agent does nothing.
 */
public class ControlledFireBrigade extends StandardAgent<FireBrigade> {
    private static final int MAX_WATER = 15000;
    private static final int EXTINGUISH_DISTANCE = 30000;
    private static final int EXTINGUISH_POWER = 1000;

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
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        if (target == null) {
            Logger.info("Nothing to do");
            return;
        }
        if (target instanceof Refuge) {
            // Just go there
            List<EntityID> path = search.breadthFirstSearch(me().getPosition(), target.getID());
            if (path != null) {
                sendMove(time, path);
                return;
            }
            else {
                Logger.info("Couldn't plan a path to refuge.");
            }
        }
        // Are we close enough to extinguish?
        int distance = model.getDistance(me(), target);
        if (distance < EXTINGUISH_DISTANCE) {
            sendExtinguish(time, target.getID(), EXTINGUISH_POWER);
            return;
        }
        // Otherwise plan a path
        if (!target.equals(location())) {
            List<EntityID> path = planPathToFire();
            if (path != null) {
                sendMove(time, path);
                return;
            }
            else {
                Logger.info("Couldn't plan a path to target.");
            }
        }
    }

    private List<EntityID> planPathToFire() {
        // Try to get to anything within EXTINGUISH_DISTANCE of the target
        Collection<StandardEntity> targets = model.getObjectsInRange(target, EXTINGUISH_DISTANCE);
        if (targets.isEmpty()) {
            return null;
        }
        return search.breadthFirstSearch(me().getPosition(), objectsToIDs(targets));
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
    }

    /**
       Get the location of the entity controlled by this agent.
       @return The location of the entity controlled by this agent.
    */
    protected StandardEntity location() {
        FireBrigade me = me();
        return me.getPosition(model);
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        search = new SampleSearch(model);
    }

    @Override
    public String toString() {
        if (me() == null) {
            return "Human controlled fire brigade";
        }
        return "Human controlled fire brigade " + me().getID() + " (" + me().getWater() + " water)" + (target == null ? " (no target)" : " target: building " + target.getID());
    }
}