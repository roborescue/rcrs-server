package rescuecore2.version0.messages;

import rescuecore2.worldmodel.EntityID;

/**
   An agent Clear command.
 */
public class AKClear extends AgentCommand {
    private EntityIDComponent target;

    /**
       Create an empty AKClear command.
     */
    AKClear() {
        super("AK_CLEAR", MessageConstants.AK_CLEAR);
        init();
    }

    /**
       Construct an AKClear command.
       @param agent The ID of the agent issuing the command.
       @param target The id of the entity to clear.
     */
    public AKClear(EntityID agent, EntityID target) {
        super("AK_CLEAR", MessageConstants.AK_CLEAR, agent);
        init();
        this.target.setValue(target);
    }

    /**
       Get the desired target.
       @return The target ID.
     */
    public EntityID getTarget() {
        return target.getValue();
    }

    private void init() {
        target = new EntityIDComponent("Target");
        addMessageComponent(target);
    }
}