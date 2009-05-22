package rescuecore2.version0.messages;

import java.util.Collection;
import java.util.List;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.EntityComponent;
import rescuecore2.messages.EntityListComponent;

import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.entities.RescueEntityType;
import rescuecore2.version0.entities.RescueEntityFactory;

/**
   A message for signalling a successful connection to the kernel.
 */
public class KAConnectOK extends AbstractMessage {
    private IntComponent tempID;
    private IntComponent agentID;
    private EntityComponent<RescueEntityType, RescueEntity> agent;
    private EntityListComponent<RescueEntityType, RescueEntity> world;

    /**
       An empty KAConnectOK message.
     */
    public KAConnectOK() {
        super("KA_CONNECT_OK", MessageConstants.KA_CONNECT_OK);
        tempID = new IntComponent("Temp ID");
        agentID = new IntComponent("Agent ID");
        agent = new EntityComponent<RescueEntityType, RescueEntity>("Agent", RescueEntityFactory.INSTANCE);
        world = new EntityListComponent<RescueEntityType, RescueEntity>("Entities", RescueEntityFactory.INSTANCE);
        addMessageComponent(tempID);
        addMessageComponent(agentID);
        addMessageComponent(agent);
        addMessageComponent(world);
    }

    /**
       A populated KAConnectOK message.
       @param tempID The tempID of the agent that has successfully connected.
       @param agentID The ID of the Entity that the agent will be controlling.
       @param object The Entity that the agent will be controlling.
       @param allEntities All other Entities that the agent knows about.
     */
    public KAConnectOK(int tempID, int agentID, RescueEntity object, Collection<RescueEntity> allEntities) {
        this();
        this.tempID.setValue(tempID);
        this.agentID.setValue(agentID);
        this.agent.setEntity(object);
        this.world.setEntities(allEntities);
    }

    /**
       Get the tempID for this message.
       @return The temp ID.
     */
    public int getTempID() {
        return tempID.getValue();
    }

    /**
       Get the ID of the agent-controlled object.
       @return The agent ID.
     */
    public int getAgentID() {
        return agentID.getValue();
    }

    /**
       Get the agent-controlled entity.
       @return The agent-controlled entity.
     */
    public RescueEntity getAgent() {
        return agent.getEntity();
    }

    /**
       Get the entity list.
       @return All entities in the world.
     */
    public List<RescueEntity> getEntities() {
        return world.getEntities();
    }
}