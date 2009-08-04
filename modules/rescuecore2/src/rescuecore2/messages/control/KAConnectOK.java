package rescuecore2.messages.control;

import java.util.Collection;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.EntityIDComponent;
import rescuecore2.messages.EntityListComponent;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
   A message for signalling a successful connection to the kernel.
 */
public class KAConnectOK extends AbstractMessage implements Control {
    private IntComponent requestID;
    private EntityIDComponent agentID;
    private EntityListComponent world;

    /**
       An empty KAConnectOK message.
     */
    public KAConnectOK() {
        super("KA_CONNECT_OK", ControlMessageConstants.KA_CONNECT_OK);
        requestID = new IntComponent("Request ID");
        agentID = new EntityIDComponent("Agent ID");
        world = new EntityListComponent("Entities");
        addMessageComponent(requestID);
        addMessageComponent(agentID);
        addMessageComponent(world);
    }

    /**
       A populated KAConnectOK message.
       @param requestID The request ID.
       @param agentID The ID of the Entity that the agent will be controlling.
       @param allEntities All Entities that the agent knows about, including the controlled object.
     */
    public KAConnectOK(int requestID, EntityID agentID, Collection<? extends Entity> allEntities) {
        this();
        this.requestID.setValue(requestID);
        this.agentID.setValue(agentID);
        this.world.setEntities(allEntities);
    }

    /**
       Get the requestID for this message.
       @return The request ID.
     */
    public int getRequestID() {
        return requestID.getValue();
    }

    /**
       Get the ID of the agent-controlled object.
       @return The agent ID.
     */
    public EntityID getAgentID() {
        return agentID.getValue();
    }

    /**
       Get the entity list.
       @return All entities in the world.
     */
    public Collection<Entity> getEntities() {
        return world.getEntities();
    }
}