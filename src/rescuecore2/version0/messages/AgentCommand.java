package rescuecore2.version0.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.Command;
import rescuecore2.messages.EntityIDComponent;

/**
   Abstract base class for all agent commands.
 */
public abstract class AgentCommand extends AbstractMessage implements Command {
    private EntityIDComponent agentID;

    /**
       Construct an empty AgentCommand.
       @param name The name of this command.
       @param typeID The typeID of this command.
     */
    protected AgentCommand(String name, int typeID) {
        super(name, typeID);
        init();
    }

    /**
       Construct an AgentCommand with an agent ID.
       @param name The name of this command.
       @param typeID The typeID of this command.
       @param agentID The ID of the agent issuing the command.
     */
    protected AgentCommand(String name, int typeID, EntityID agentID) {
        super(name, typeID);
        init();
        this.agentID.setValue(agentID);
    }

    private void init() {
        agentID = new EntityIDComponent("Agent ID");
        addMessageComponent(agentID);
    }

    @Override
    public EntityID getAgentID() {
        return agentID.getValue();
    }

    /**
       Set the ID of the agent issuing the command.
       @param agentID The new agent ID.
     */
    protected void setAgentID(EntityID agentID) {
        this.agentID.setValue(agentID);
    }
}