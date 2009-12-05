package human;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.components.StandardAgent;

import sample.SampleSearch;

import java.util.List;
import java.util.Collection;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A basic fire brigade agent that will try to extinguish a given target. If the target is a refuge then the fire brigade will attempt to enter the building to replenish water. If there is no target then this agent does nothing.
 */
public class ControlledFireBrigade extends StandardAgent<FireBrigade> {
    private static final Log LOG = LogFactory.getLog(ControlledFireBrigade.class);

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
    protected void think(int time, Collection<EntityID> changed, Collection<Command> heard) {
        if (target == null) {
            LOG.info(me() + " has nothing to do");
            return;
        }
        if (target instanceof Refuge) {
            // Just go there
            List<EntityID> path = search.breadthFirstSearch(location(), target);
            if (path != null) {
                sendMove(time, path);
                return;
            }
            else {
                LOG.info(me() + " couldn't plan a path to refuge.");
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
                LOG.info(me() + " couldn't plan a path to target.");
            }
        }
    }

    private List<EntityID> planPathToFire() {
        // Try to get to anything within EXTINGUISH_DISTANCE of the target
        Collection<StandardEntity> targets = model.getObjectsInRange(target, EXTINGUISH_DISTANCE);
        if (targets.isEmpty()) {
            return null;
        }
        return search.breadthFirstSearch(location(), targets);
    }

    @Override
    public String[] getRequestedEntityURNs() {
        return new String[] {StandardEntityURN.FIRE_BRIGADE.name()};
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
        search = new SampleSearch(model, true);
    }

    @Override
    public String toString() {
        if (me() == null) {
            return "Human controlled fire brigade";
        }
        return "Human controlled fire brigade " + me().getID() + " (" + me().getWater() + " water)" + (target == null ? " (no target)" : " target: building " + target.getID());
    }
}

