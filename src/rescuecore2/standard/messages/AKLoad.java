package rescuecore2.standard.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.EntityIDComponent;
import rescuecore2.messages.AbstractCommand;

/**
   An agent Load command.
 */
public class AKLoad extends AbstractCommand {
    private EntityIDComponent target;

    /**
       Create an empty AKLoad command.
     */
    AKLoad() {
        super("AK_LOAD", MessageConstants.AK_LOAD);
        init();
    }

    /**
       Construct an AKLoad command.
       @param agent The ID of the agent issuing the command.
       @param target The id of the entity to load.
       @param time The time the command was issued.
     */
    public AKLoad(EntityID agent, EntityID target, int time) {
        super("AK_LOAD", MessageConstants.AK_LOAD, agent, time);
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