package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.EntityListComponent;
import rescuecore2.worldmodel.Entity;

import java.util.Collection;

import java.io.InputStream;
import java.io.IOException;

/**
   A broadcast update from the kernel.
 */
public class Update extends AbstractMessage implements Control {
    private IntComponent id;
    private IntComponent time;
    private EntityListComponent updates;

    /**
       An Update message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public Update(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       A populated Update message.
       @param id The id of the simulator or viewer receiving the update.
       @param time The timestep of the simulation.
       @param updates All updated entities.
     */
    public Update(int id, int time, Collection<? extends Entity> updates) {
        this();
        this.id.setValue(id);
        this.time.setValue(time);
        this.updates.setEntities(updates);
    }

    private Update() {
        super("Update", ControlMessageConstants.UPDATE);
        id = new IntComponent("ID");
        time = new IntComponent("Time");
        updates = new EntityListComponent("Updates");
        addMessageComponent(id);
        addMessageComponent(time);
        addMessageComponent(updates);
    }

    /**
       Get the id of the component that this message is addressed to.
       @return The ID of the target component.
     */
    public int getTargetID() {
        return id.getValue();
    }

    /**
       Get the time of the simulation.
       @return The simulation time.
     */
    public int getTime() {
        return time.getValue();
    }

    /**
       Get the list of updated entities.
       @return The updated entities.
     */
    public Collection<Entity> getUpdatedEntities() {
        return updates.getEntities();
    }
}