package human;

import rescuecore2.components.AbstractAgent;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKMove;

import sample.SampleSearch;

import java.util.List;

/**
   A basic police force agent that will try to clear a given target. Fully-blocked roads encountered along the way are also cleared. If there is no target then this agent does nothing.
*/
public class ControlledPoliceForce extends AbstractAgent {
    private StandardWorldModel world;
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
    protected void think(int time, List c) {
        if (location() instanceof Road) {
            Road r = (Road)location();
            if (r.isLinesToHeadDefined() && (r.countBlockedLanes() == r.getLinesToHead())) {
                System.out.println(me() + " clearing road " + r.getID());
                AKClear clear = new AKClear(getID(), time, r.getID());
                send(clear);
                return;
            }
        }
        if (target == null) {
            System.out.println(me() + " has nothing to do.");
            return;
        }
        if (location().equals(target)) {
            if (target.isBlockDefined() && target.getBlock() == 0) {
                System.out.println(me() + ": target is clear");
                target = null;
                return;
            }
            else {
                System.out.println(me() + " clearing target road " + target.getID());
                AKClear clear = new AKClear(getID(), time, target.getID());
                send(clear);
                return;
            }
        }
        else {
            List<EntityID> path = search.breadthFirstSearch(location(), target);
            if (path != null) {
                AKMove move = new AKMove(getID(), time, path);
                System.out.println(me() + " moving to target: " + move);
                send(move);
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
        search = new SampleSearch(world, false);
    }

    @Override
    public String toString() {
        if (me() == null) {
            return "Human controlled police force";
        }
        return "Human controlled police force " + getID() + (target == null ? " (no target)" : " target: road " + target.getID() + " block = " + (target.isBlockDefined() ? " unknown" : String.valueOf(target.getBlock())));
    }
}

