package rescuecore2.messages.control;

import java.util.Collection;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.EntityListComponent;
import rescuecore2.worldmodel.Entity;

import java.io.InputStream;
import java.io.IOException;

/**
   A message for signalling a successful connection to the kernel.
 */
public class KVConnectOK extends AbstractMessage implements Control {
    private IntComponent viewerID;
    private IntComponent requestID;
    private EntityListComponent world;

    /**
       A KVConnectOK message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public KVConnectOK(InputStream in) throws IOException {
        this();
        read(in);
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

    private KVConnectOK() {
        super("KV_CONNECT_OK", ControlMessageConstants.KV_CONNECT_OK);
        viewerID = new IntComponent("Viewer ID");
        requestID = new IntComponent("Request ID");
        world = new EntityListComponent("Entities");
        addMessageComponent(requestID);
        addMessageComponent(viewerID);
        addMessageComponent(world);
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
    public Collection<Entity> getEntities() {
        return world.getEntities();
    }
}