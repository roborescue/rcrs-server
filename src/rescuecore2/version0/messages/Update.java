package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;

import rescuecore2.version0.entities.RescueObject;

import java.util.Collection;
import java.util.List;

/**
   A broadcast update from the kernel.
 */
public class Update extends AbstractMessage {
    private IntComponent time;
    private EntityListComponent updates;

    /**
       An empty Update message.
     */
    public Update() {
        super("Update", MessageConstants.UPDATE);
        time = new IntComponent("Time");
        updates = new EntityListComponent("Updates");
        addMessageComponent(time);
        addMessageComponent(updates);
    }

    /**
       A populated Update message.
       @param time The timestep of the simulation.
       @param updates All updated entities.
     */
    public Update(int time, Collection<RescueObject> updates) {
        this();
        this.time.setValue(time);
        this.updates.setEntities(updates);
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
    public List<RescueObject> getUpdatedEntities() {
        return updates.getEntities();
    }
}