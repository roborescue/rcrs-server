package rescuecore2.version0.messages;

import java.util.Collection;
import java.util.List;

import rescuecore2.messages.AbstractMessage;

import rescuecore2.version0.entities.RescueObject;

/**
   A message for signalling a successful connection to the kernel.
 */
public class KSConnectOK extends AbstractMessage {
    private IntComponent simulatorID;
    private EntityListComponent world;

    /**
       An empty KSConnectOK message.
     */
    public KSConnectOK() {
        super("KS_CONNECT_OK", MessageConstants.KS_CONNECT_OK);
        simulatorID = new IntComponent("Simulator ID");
        world = new EntityListComponent("Entities");
        addMessageComponent(simulatorID);
        addMessageComponent(world);
    }

    /**
       A populated KSConnectOK message.
       @param simulatorID The ID of the simulator that has successfully connected.
       @param allEntities All Entities in the world.
     */
    public KSConnectOK(int simulatorID, Collection<RescueObject> allEntities) {
        this();
        this.simulatorID.setValue(simulatorID);
        this.world.setEntities(allEntities);
    }

    /**
       Get the simulator ID for this message.
       @return The simulator ID.
     */
    public int getSimulatorID() {
        return simulatorID.getValue();
    }

    /**
       Get the entity list.
       @return All entities in the world.
     */
    public List<RescueObject> getEntities() {
        return world.getEntities();
    }
}