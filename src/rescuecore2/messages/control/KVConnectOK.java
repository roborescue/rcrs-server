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
public class KVConnectOK extends AbstractMessage implements Control {
    private IntComponent viewerID;
    private IntComponent requestID;
    private EntityListComponent world;

    /**
       An empty KVConnectOK message.
     */
    public KVConnectOK() {
        super("KV_CONNECT_OK", ControlMessageConstants.KV_CONNECT_OK);
        viewerID = new IntComponent("Viewer ID");
        requestID = new IntComponent("Request ID");
        world = new EntityListComponent("Entities");
        addMessageComponent(requestID);
        addMessageComponent(viewerID);
        addMessageComponent(world);
    }

    /**
       A populated KVConnectOK message.
       @param viewerID The viewer ID.
       @param requestID The request ID.
       @param allEntities All Entities in the world.
     */
    public KVConnectOK(int viewerID, int requestID, Collection<? extends Entity> allEntities) {
        this();
        this.viewerID.setValue(viewerID);
        this.requestID.setValue(requestID);
        this.world.setEntities(allEntities);
    }

    /**
       Get the viewer ID.
       @return The viewer ID.
     */
    public int getViewerID() {
        return viewerID.getValue();
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