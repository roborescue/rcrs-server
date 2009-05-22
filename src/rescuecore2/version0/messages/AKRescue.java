package rescuecore2.version0.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.EntityIDComponent;

/**
   An agent Rescue command.
 */
public class AKRescue extends AgentCommand {
    private EntityIDComponent target;

    /**
       Create an empty AKRescue command.
     */
    AKRescue() {
        super("AK_RESCUE", MessageConstants.AK_RESCUE);
        init();
    }

    /**
       Construct an AKRescue command.
       @param agent The ID of the agent issuing the command.
       @param target The id of the entity to rescue.
     */
    public AKRescue(EntityID agent, EntityID target) {
        super("AK_RESCUE", MessageConstants.AK_RESCUE, agent);
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