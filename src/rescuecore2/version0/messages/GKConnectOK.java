package rescuecore2.version0.messages;

import java.util.List;

import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.version0.messages.EntityListComponent;

/**
   A message for signalling a successful connection to the GIS.
 */
public class GKConnectOK extends AbstractMessage {
    private EntityListComponent world;

    public GKConnectOK() {
        super("GK_CONNECT_OK", MessageConstants.GK_CONNECT_OK);
        world = new EntityListComponent("Entities");
        addMessageComponent(world);
    }

    /**
       Get the entity list.
       @return All entities.
     */
    public List<Entity> getEntities() {
        return world.getEntities();
    }
}