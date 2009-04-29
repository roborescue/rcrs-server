package rescuecore2.version0.messages;

import java.util.Collection;
import java.util.List;

import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.AbstractMessage;

/**
   A message for signalling a successful connection to the kernel.
 */
public class KVConnectOK extends AbstractMessage {
    private EntityListComponent world;

    /**
       An empty KVConnectOK message.
     */
    public KVConnectOK() {
        super("KV_CONNECT_OK", MessageConstants.KV_CONNECT_OK);
        world = new EntityListComponent("Entities");
        addMessageComponent(world);
    }

    /**
       A populated KVConnectOK message.
       @param allEntities All Entities in the world.
     */
    public KVConnectOK(Collection<Entity> allEntities) {
        this();
        this.world.setEntities(allEntities);
    }

    /**
       Get the entity list.
       @return All entities in the world.
     */
    public List<Entity> getEntities() {
        return world.getEntities();
    }
}