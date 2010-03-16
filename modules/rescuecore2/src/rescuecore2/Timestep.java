package rescuecore2;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import rescuecore2.messages.Command;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
   A record of everything that happened in a timestep. This includes agent perception, commands and world model updates.
*/
public class Timestep {
    private int time;
    private Collection<Command> commands;
    private ChangeSet changes;
    private Map<EntityID, ChangeSet> agentPerception;
    private Map<EntityID, Collection<Command>> agentHearing;
    private double score;

    /**
       Construct a timestep record.
       @param time The timestep number.
    */
    public Timestep(int time) {
        this.time = time;
        agentPerception = new HashMap<EntityID, ChangeSet>();
        agentHearing = new HashMap<EntityID, Collection<Command>>();
        commands = new ArrayList<Command>();
    }

    /**
       Set the commands for this timestep.
       @param c The commands.
    */
    public void setCommands(Collection<Command> c) {
        commands.clear();
        commands.addAll(c);
    }

    /**
       Set the simulator updates ChangeSet for this timestep.
       @param c The ChangeSet.
    */
    public void setChangeSet(ChangeSet c) {
        this.changes = c;
    }

    /**
       Register agent perception.
       @param id The agent ID.
       @param perception The ChangeSet the entity can perceive.
       @param hearing The messages the agent heard.
    */
    public void registerPerception(EntityID id, ChangeSet perception, Collection<Command> hearing) {
        agentPerception.put(id, perception);
        agentHearing.put(id, hearing);
    }

    /**
       Set the score for this timestep.
       @param s The score.
    */
    public void setScore(double s) {
        score = s;
    }

    /**
       Get the time.
       @return The time.
    */
    public int getTime() {
        return time;
    }

    /**
       Get the commands sent by agents this timestep.
       @return The commands.
    */
    public Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(commands);
    }

    /**
       Get the commands sent by a particular agent this timestep.
       @param agentID The ID of the agent.
       @return All commands send by that agent.
    */
    public Collection<Command> getCommands(EntityID agentID) {
        Set<Command> result = new HashSet<Command>();
        for (Command next : commands) {
            if (next.getAgentID().equals(agentID)) {
                result.add(next);
            }
        }
        return result;
    }

    /**
       Get the changes to entities during this timestep.
       @return The changes.
    */
    public ChangeSet getChangeSet() {
        return changes;
    }

    /**
       Get the set of agent IDs for agents that received an update this timestep.
       @return The IDs of all agents that received an update.
     */
    public Set<EntityID> getAgentsWithUpdates() {
        Set<EntityID> result = new HashSet<EntityID>();
        result.addAll(agentPerception.keySet());
        result.addAll(agentHearing.keySet());
        return result;
    }

    /**
       Get the changes to entities that an agent saw at the start of this timestep.
       @param agentID The agent ID to look up.
       @return The ChangeSet the agent saw, or null if the ID is not recognised.
    */
    public ChangeSet getAgentPerception(EntityID agentID) {
        return agentPerception.get(agentID);
    }

    /**
       Get the communication messages that an agent heard at the start of this timestep.
       @param agentID The agent ID to look up.
       @return The set of messages the agent heard, or null if the ID is not recognised.
    */
    public Collection<Command> getAgentHearing(EntityID agentID) {
        return agentHearing.get(agentID);
    }

    /**
       Get the score for this timestep.
       @return The score.
    */
    public double getScore() {
        return score;
    }
}
