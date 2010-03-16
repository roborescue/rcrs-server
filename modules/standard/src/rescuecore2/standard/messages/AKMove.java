package rescuecore2.standard.messages;

import java.util.List;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.components.EntityIDListComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.AbstractCommand;

import java.io.InputStream;
import java.io.IOException;

/**
   An agent move command.
 */
public class AKMove extends AbstractCommand {
    private EntityIDListComponent path;
    private IntComponent x;
    private IntComponent y;

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
       @param time The time the command was issued.
       @param agent The ID of the agent issuing the command.
       @param path The path to move.
     */
    public AKMove(EntityID agent, int time, List<EntityID> path) {
        this();
        setAgentID(agent);
        setTime(time);
        this.path.setIDs(path);
        this.x.setValue(-1);
        this.y.setValue(-1);
    }

    /**
       Construct a move command.
       @param time The time the command was issued.
       @param agent The ID of the agent issuing the command.
       @param path The path to move.
       @param destinationX The X coordinate of the desired destination.
       @param destinationY The Y coordinate of the desired destination.
     */
    public AKMove(EntityID agent, int time, List<EntityID> path, int destinationX, int destinationY) {
        this();
        setAgentID(agent);
        setTime(time);
        this.path.setIDs(path);
        this.x.setValue(destinationX);
        this.y.setValue(destinationY);
    }

    private AKMove() {
        super(StandardMessageURN.AK_MOVE);
        path = new EntityIDListComponent("Path");
        x = new IntComponent("Destination X");
        y = new IntComponent("Destination Y");
        addMessageComponent(path);
        addMessageComponent(x);
        addMessageComponent(y);
    }

    /**
       Get the desired movement path.
       @return The movement path.
     */
    public List<EntityID> getPath() {
        return path.getIDs();
    }

    /**
       Get the destination X coordinate.
       @return The destination X coordination.
    */
    public int getDestinationX() {
        return x.getValue();
    }

    /**
       Get the destination Y coordinate.
       @return The destination Y coordination.
    */
    public int getDestinationY() {
        return y.getValue();
    }
}
