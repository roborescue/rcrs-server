package rescuecore2.messages.control;

import java.util.Collection;
import java.util.List;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.EntityIDComponent;
import rescuecore2.messages.EntityListComponent;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
   A message for signalling a perception update for an agent.
 */
public class KASense extends AbstractMessage implements Control {
    private EntityIDComponent agentID;
    private IntComponent time;
    private EntityListComponent updates;

    /**
       An empty KASense message.
     */
    public KASense() {
        super("KA_SENSE", ControlMessageConstants.KA_SENSE);
        agentID = new EntityIDComponent("Agent ID");
        time = new IntComponent("Time");
        updates = new EntityListComponent("Updates");
        addMessageComponent(agentID);
        addMessageComponent(time);
        addMessageComponent(updates);
    }

    /**
       A populated KASense message.
       @param agentID The ID of the Entity that is receiving the update.
       @param time The timestep of the simulation.
       @param updates All Entities that the agent can perceive.
     */
    public KASense(EntityID agentID, int time, Collection<? extends Entity> updates) {
        this();
        this.agentID.setValue(agentID);
        this.time.setValue(time);
        this.updates.setEntities(updates);
    }

    /**
       Get the ID of the agent.
       @return The agent ID.
     */
    public EntityID getAgentID() {
        return agentID.getValue();
    }

    /**
       Get the time.
       @return The time.
     */
    public int getTime() {
        return time.getValue();
    }

    /**
       Get the updated entity list.
       @return All entities that have been updated.
     */
    public List<Entity> getUpdates() {
        return updates.getEntities();
    }
}