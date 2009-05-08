package rescuecore2.version0.messages;

import java.util.List;

import rescuecore2.worldmodel.EntityID;

/**
   An agent MOVE command.
 */
public class AKMove extends AgentCommand {
    private EntityIDListComponent path;

    /**
       Construct a move command with no path information.
     */
    public AKMove() {
        super("AK_MOVE", MessageConstants.AK_MOVE);
        path = new EntityIDListComponent("Path");
        addMessageComponent(path);
    }

    /**
       Construct a move command with path information.
       @param path The path to move.
     */
    public AKMove(List<EntityID> path) {
        this();
        this.path.setIDs(path);
    }

    /**
       Get the desired movement path.
       @return The movement path.
     */
    public List<EntityID> getPath() {
        return path.getIDs();
    }
}