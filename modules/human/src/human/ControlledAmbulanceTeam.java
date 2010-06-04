package human;

import static rescuecore2.misc.Handy.objectsToIDs;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.components.StandardAgent;

import sample.SampleSearch;

import java.util.Collection;
import java.util.List;
import java.util.EnumSet;

/**
   A basic ambulance team agent that will try to rescue a given target. Once the target is unburied this agent will attempt to load it and transport it to a refuge. If there is no target then this agent does nothing.
 */
public class ControlledAmbulanceTeam extends StandardAgent<AmbulanceTeam> {
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
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        if (target == null) {
            Logger.info("Nothing to do.");
            return;
        }
        else {
            // Is the target on board?
            if (target.getPosition().equals(getID())) {
                // Yes
                // Are we at a  refuge?
                if (location() instanceof Refuge) {
                    sendUnload(time);
                    return;
                }
                else {
                    List<EntityID> path = search.breadthFirstSearch(me().getPosition(), objectsToIDs(model.getEntitiesOfType(StandardEntityURN.REFUGE)));
                    if (path != null) {
                        sendMove(time, path);
                        return;
                    }
                    else {
                        Logger.info("Couldn't plan a path to refuge.");
                        return;
                    }
                }
            }
            else {
                if (target.getPosition().equals(me().getPosition())) {
                    // We're at the same location
                    if (target.getBuriedness() != 0) {
                        sendRescue(time, target.getID());
                        return;
                    }
                    else {
                        // Unburied: try to load
                        sendLoad(time, target.getID());
                        return;
                    }
                }
                else {
                    // Plan a path
                    List<EntityID> path = search.breadthFirstSearch(me().getPosition(), target.getID());
                    if (path != null) {
                        sendMove(time, path);
                        return;
                    }
                    else {
                        Logger.info("Couldn't plan a path to target.");
                    }
                }
            }
        }
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
    }

    /**
       Get the location of the entity controlled by this agent.
       @return The location of the entity controlled by this agent.
    */
    protected StandardEntity location() {
        AmbulanceTeam me = me();
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

