package rescuecore2.standard.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.AbstractCommand;

/**
   An agent Unload command.
 */
public class AKUnload extends AbstractCommand {
    /**
       Construct an empty unload command.
     */
    AKUnload() {
        super("AK_UNLOAD", MessageConstants.AK_UNLOAD);
    }

    /**
       Construct an unload command.
       @param agentID The ID of the agent issuing the command.
       @param time The time the command was issued.
     */
    public AKUnload(EntityID agentID, int time) {
        super("AK_UNLOAD", MessageConstants.AK_UNLOAD, agentID, time);
    }
}