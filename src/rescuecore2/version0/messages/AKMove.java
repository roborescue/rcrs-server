package rescuecore2.version0.messages;

import java.util.List;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.EntityIDListComponent;

/**
   An agent MOVE command.
 */
public class AKMove extends AgentCommand {
    private EntityIDListComponent path;

    /**
       Create an empty AKMove command.
     */
    AKMove() {
        super("AK_MOVE", MessageConstants.AK_MOVE);
        init();
    }

    /**
       Construct a move command.
       @param agent The ID of the agent issuing the command.
       @param path The path to move.
       @param time The time the command was issued.
     */
    public AKMove(EntityID agent, List<EntityID> path, int time) {
        super("AK_MOVE", MessageConstants.AK_MOVE, agent, time);
        init();
        this.path.setIDs(path);
    }

    /**
       Get the desired movement path.
       @return The movement path.
     */
    public List<EntityID> getPath() {
        return path.getIDs();
    }

    private void init() {
        path = new EntityIDListComponent("Path");
        addMessageComponent(path);
    }
}