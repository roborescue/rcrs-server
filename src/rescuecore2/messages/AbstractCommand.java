package rescuecore2.messages;

import rescuecore2.worldmodel.EntityID;

/**
   A sub-interface of Message that tags messages that are interpreted as agent commands.
 */
public abstract class AbstractCommand extends AbstractMessage implements Command {
    private EntityIDComponent agentID;
    private IntComponent time;

    /**
       Construct a new abstract command.
       @param name The name of the command.
       @param typeID The type ID of the command.
     */
    protected AbstractCommand(String name, int typeID) {
        super(name, typeID);
        agentID = new EntityIDComponent("Agent ID");
        time = new IntComponent("Time");
        addMessageComponent(agentID);
        addMessageComponent(time);
    }

    /**
       Construct a new abstract command.
       @param name The name of the command.
       @param typeID The type ID of the command.
       @param agentID The ID of the agent issuing the command.
       @param time The time this command was issued.
     */
    protected AbstractCommand(String name, int typeID, EntityID agentID, int time) {
        this(name, typeID);
        setAgentID(agentID);
        setTime(time);
    }

    @Override
    public EntityID getAgentID() {
        return agentID.getValue();
    }

    @Override
    public int getTime() {
        return time.getValue();
    }

    /**
       Set the ID of the agent issuing the command.
       @param agentID The new agent ID.
     */
    protected void setAgentID(EntityID agentID) {
        this.agentID.setValue(agentID);
    }

    /**
       Set the time of the command.
       @param time The new time.
     */
    protected void setTime(int time) {
        this.time.setValue(time);
    }
}