package rescuecore2.messages;

import rescuecore2.messages.components.EntityIDComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.worldmodel.EntityID;

/**
   A sub-interface of Message that tags messages that are interpreted as agent commands.
 */
public abstract class AbstractCommand extends AbstractMessage implements Command {
    private EntityIDComponent agentID;
    private IntComponent time;

    /**
       Construct a new abstract command.
       @param urn The urn of the command.
     */
    protected AbstractCommand(String urn) {
        super(urn);
        init();
    }

    /**
       Construct a new abstract command.
       @param urn The urn of the command.
       @param agentID The ID of the agent issuing the command.
       @param time The time this command was issued.
     */
    protected AbstractCommand(String urn, EntityID agentID, int time) {
        super(urn);
        init(agentID, time);
    }

    /**
       Construct a new abstract command.
       @param urn The urn of the command.
     */
    protected AbstractCommand(Enum<?> urn) {
        super(urn);
        init();
    }

    /**
       Construct a new abstract command.
       @param urn The urn of the command.
       @param agentID The ID of the agent issuing the command.
       @param time The time this command was issued.
     */
    protected AbstractCommand(Enum<?> urn, EntityID agentID, int time) {
        super(urn);
        init(agentID, time);
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

    private void init() {
        agentID = new EntityIDComponent("Agent ID");
        time = new IntComponent("Time");
        addMessageComponent(agentID);
        addMessageComponent(time);
    }

    private void init(EntityID id, int t) {
        init();
        setAgentID(id);
        setTime(t);
    }
}
