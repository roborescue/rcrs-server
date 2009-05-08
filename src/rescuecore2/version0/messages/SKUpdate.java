package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;

import rescuecore2.version0.entities.RescueObject;

import java.util.List;

/**
   A message for sending updates from a simulator to the kernel.
 */
public class SKUpdate extends AbstractMessage {
    private IntComponent id;
    private IntComponent time;
    private EntityListComponent update;

    /**
       SKUpdate message with a undefined data.
     */
    public SKUpdate() {
        super("SK_UPDATE", MessageConstants.SK_UPDATE);
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
       @param data The updated objects.
     */
    public SKUpdate(int id, List<RescueObject> data) {
        this();
        this.id.setValue(id);
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
    public List<RescueObject> getUpdatedEntities() {
        return update.getEntities();
    }
}