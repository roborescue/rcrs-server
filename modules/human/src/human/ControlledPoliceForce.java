package human;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.components.StandardAgent;

import sample.SampleSearch;

import java.util.Collection;
import java.util.List;

/**
   A basic police force agent that will try to clear a given target. Fully-blocked roads encountered along the way are also cleared. If there is no target then this agent does nothing.
*/
public class ControlledPoliceForce extends StandardAgent<PoliceForce> {
    private SampleSearch search;
    private Road target;

    /**
       Set the target for this police force.
       @param target The new target.
    */
    public void setTarget(Road target) {
        this.target = target;
    }

    @Override
    protected void think(int time, Collection<EntityID> changed, Collection<Command> heard) {
        if (location() instanceof Road) {
            Road r = (Road)location();
            if (r.isLinesToHeadDefined() && (r.countBlockedLanes() == r.getLinesToHead())) {
                sendClear(time, r.getID());
                return;
            }
        }
        if (target == null) {
            System.out.println(me() + " has nothing to do.");
            return;
        }
        if (location().equals(target)) {
            if (target.isBlockDefined() && target.getBlock() == 0) {
                target = null;
                return;
            }
            else {
                sendClear(time, target.getID());
                return;
            }
        }
        else {
            List<EntityID> path = search.breadthFirstSearch(location(), target);
            if (path != null) {
                sendMove(time, path);
                return;
            }
            else {
                System.out.println(me() + " couldn't plan a path to target.");
            }
        }
    }

    @Override
    public String[] getRequestedEntityURNs() {
        return new String[] {StandardEntityURN.POLICE_FORCE.name()};
    }

    /**
       Get the location of the entity controlled by this agent.
       @return The location of the entity controlled by this agent.
    */
    protected StandardEntity location() {
        PoliceForce me = me();
        return me.getPosition(model);
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        search = new SampleSearch(model, false);
    }

    @Override
    public String toString() {
        if (me() == null) {
            return "Human controlled police force";
        }
        return "Human controlled police force " + getID() + (target == null ? " (no target)" : " target: road " + target.getID() + " block = " + (target.isBlockDefined() ? " unknown" : String.valueOf(target.getBlock())));
    }
}

