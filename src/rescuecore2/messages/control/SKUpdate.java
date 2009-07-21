package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.EntityListComponent;
import rescuecore2.worldmodel.Entity;

import java.util.List;
import java.util.Collection;

/**
   A message for sending updates from a simulator to the kernel.
 */
public class SKUpdate extends AbstractMessage implements Control {
    private IntComponent id;
    private IntComponent time;
    private EntityListComponent update;

    /**
       SKUpdate message with a undefined data.
     */
    public SKUpdate() {
        super("SK_UPDATE", ControlMessageConstants.SK_UPDATE);
        id = new IntComponent("ID");
        time = new IntComponent("Time");
        update = new EntityListComponent("Updated objects");
        addMessageComponent(id);
        addMessageComponent(time);
        addMessageComponent(update);
    }

    /**
       SKUpdate message with a specific ID and data component.
       @param id The id of the simulator sending the update.
       @param time The timestep this update refers to.
       @param data The updated objects.
     */
    public SKUpdate(int id, int time, Collection<? extends Entity> data) {
        this();
        this.id.setValue(id);
        this.time.setValue(time);
        this.update.setEntities(data);
    }

    /**
       Get the ID of the simulator that is acknowledging the connection.
       @return The simulator ID component.
     */
    public int getSimulatorID() {
        return id.getValue();
    }

    /**
       Get the updated entity list.
       @return The updated entity list.
     */
    public List<Entity> getUpdatedEntities() {
        return update.getEntities();
    }

    /**
       Get the timestep this update is for.
       @return The timestep.
     */
    public int getTime() {
        return time.getValue();
    }
}