package rescuecore2.messages.control;

import java.util.Collection;
import java.util.List;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.EntityListComponent;
import rescuecore2.worldmodel.Entity;

/**
   A message for signalling a successful connection to the kernel.
 */
public class KSConnectOK extends AbstractMessage implements Control {
    private IntComponent simulatorID;
    private IntComponent requestID;
    private EntityListComponent world;

    /**
       An empty KSConnectOK message.
    */
    public KSConnectOK() {
        super("KS_CONNECT_OK", ControlMessageConstants.KS_CONNECT_OK);
        simulatorID = new IntComponent("Simulator ID");
        requestID = new IntComponent("Request ID");
        world = new EntityListComponent("Entities");
        addMessageComponent(requestID);
        addMessageComponent(simulatorID);
        addMessageComponent(world);
    }

    /**
       A populated KSConnectOK message.
       @param simulatorID The ID of the simulator that has successfully connected.
       @param requestID The request ID.
       @param allEntities All Entities in the world.
    */
    public KSConnectOK(int simulatorID, int requestID, Collection<? extends Entity> allEntities) {
        this();
        this.simulatorID.setValue(simulatorID);
        this.requestID.setValue(requestID);
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
       Get the request ID.
       @return The request ID.
     */
    public int getRequestID() {
        return requestID.getValue();
    }

    /**
       Get the entity list.
       @return All entities in the world.
    */
    public List<Entity> getEntities() {
        return world.getEntities();
    }
}