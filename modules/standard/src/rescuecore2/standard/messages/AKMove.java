package rescuecore2.standard.messages;

import java.util.List;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.EntityIDListComponent;
import rescuecore2.messages.AbstractCommand;

import java.io.InputStream;
import java.io.IOException;

/**
   An agent move command.
 */
public class AKMove extends AbstractCommand {
    private EntityIDListComponent path;

    /**
       An AKMove message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public AKMove(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       Construct a move command.
       @param agent The ID of the agent issuing the command.
       @param path The path to move.
       @param time The time the command was issued.
     */
    public AKMove(EntityID agent, int time, List<EntityID> path) {
        this();
        setAgentID(agent);
        setTime(time);
        this.path.setIDs(path);
    }

    private AKMove() {
        super("AK_MOVE", MessageConstants.AK_MOVE);
        path = new EntityIDListComponent("Path");
        addMessageComponent(path);
    }

    /**
       Get the desired movement path.
       @return The movement path.
     */
    public List<EntityID> getPath() {
        return path.getIDs();
    }
}