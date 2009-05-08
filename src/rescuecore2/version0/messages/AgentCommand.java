package rescuecore2.version0.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.AbstractMessage;

/**
   Abstract base class for all agent commands.
 */
public abstract class AgentCommand extends AbstractMessage {
    private EntityIDComponent agentID;

    /**
       An AgentCommand with undefined values.
       @param name The name of this command.
       @param typeID The typeID of this command.
     */
    protected AgentCommand(String name, int typeID) {
        super(name, typeID);
        agentID = new EntityIDComponent("Agent ID");
        addMessageComponent(agentID);
    }

    /**
       An AgentCommand with particular agentID.
       @param name The name of this command.
       @param typeID The typeID of this command.
       @param agentID The agent ID.
     */
    protected AgentCommand(String name, int typeID, EntityID agentID) {
        this(name, typeID);
        this.agentID.setValue(agentID);
    }

    /**
       Get the ID of the agent issuing the command.
       @return The agent ID.
     */
    public EntityID getAgentID() {
        return agentID.getValue();
    }
}