package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.EntityListComponent;

import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.entities.RescueEntityType;
import rescuecore2.version0.entities.RescueEntityFactory;

import java.util.Collection;
import java.util.List;

/**
   A broadcast update from the kernel.
 */
public class Update extends AbstractMessage {
    private IntComponent time;
    private EntityListComponent<RescueEntityType, RescueEntity> updates;

    /**
       An empty Update message.
     */
    public Update() {
        super("Update", MessageConstants.UPDATE);
        time = new IntComponent("Time");
        updates = new EntityListComponent<RescueEntityType, RescueEntity>("Updates", RescueEntityFactory.INSTANCE);
        addMessageComponent(time);
        addMessageComponent(updates);
    }

    /**
       A populated Update message.
       @param time The timestep of the simulation.
       @param updates All updated entities.
     */
    public Update(int time, Collection<RescueEntity> updates) {
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
    public List<RescueEntity> getUpdatedEntities() {
        return updates.getEntities();
    }
}