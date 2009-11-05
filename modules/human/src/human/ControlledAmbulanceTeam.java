package human;

import rescuecore2.components.AbstractAgent;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.messages.AKRescue;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.standard.messages.AKUnload;
import rescuecore2.standard.messages.AKMove;

import sample.SampleSearch;

import java.util.Collection;
import java.util.List;

/**
   A basic ambulance team agent that will try to rescue a given target. Once the target is unburied this agent will attempt to load it and transport it to a refuge. If there is no target then this agent does nothing.
 */
public class ControlledAmbulanceTeam extends AbstractAgent {
    private StandardWorldModel world;
    private SampleSearch search;
    private Human target;

    /**
       Set the target of this ambulance team.
       @param target The new target.
     */
    public void setTarget(Human target) {
        this.target = target;
    }

    @Override
    protected void think(int time, Collection c) {
        if (target == null) {
            System.out.println(me() + " has nothing to do.");
            return;
        }
        else {
            // Is the target on board?
            if (target.getPosition().equals(getID())) {
                // Yes
                // Are we at a  refuge?
                if (location() instanceof Refuge) {
                    AKUnload unload = new AKUnload(getID(), time);
                    System.out.println(me() + " unloading");
                    send(unload);
                    return;
                }
                else {
                    List<EntityID> path = search.breadthFirstSearch(location(), world.getEntitiesOfType(StandardEntityURN.REFUGE));
                    if (path != null) {
                        AKMove move = new AKMove(getID(), time, path);
                        System.out.println(me() + " moving to refuge: " + move);
                        send(move);
                        return;
                    }
                    else {
                        System.out.println(me() + " couldn't plan a path to refuge!");
                        return;
                    }
                }
            }
            else {
                if (target.getPosition().equals(((Human)me()).getPosition())) {
                    // We're at the same location
                    if (target.getBuriedness() != 0) {
                        AKRescue rescue = new AKRescue(getID(), time, target.getID());
                        System.out.println(me() + " rescueing target " + target);
                        send(rescue);
                        return;
                    }
                    else {
                        // Unburied: try to load
                        AKLoad load = new AKLoad(getID(), time, target.getID());
                        System.out.println(me() + " loading target " + target);
                        send(load);
                        return;
                    }
                }
                else {
                    // Plan a path
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
        }
    }

    @Override
    public String[] getRequestedEntityURNs() {
        return new String[] {StandardEntityURN.AMBULANCE_TEAM.name()};
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
        if (me() == null) {
            return "Human controlled ambulance team";
        }
        StringBuilder result = new StringBuilder();
        result.append("Human controlled ambulance team ");
        result.append(getID());
        result.append(" ");
        if (target == null) {
            result.append("(no target)");
        }
        else {
            result.append("target: human ");
            result.append(target.getID());
            if (target.getPosition().equals(getID())) {
                result.append(" (loaded)");
            }
        }
        return result.toString();
    }
}

