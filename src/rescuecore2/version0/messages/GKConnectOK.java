package rescuecore2.version0.messages;

import java.util.List;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.EntityListComponent;

import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.entities.RescueEntityType;
import rescuecore2.version0.entities.RescueEntityFactory;

/**
   A message for signalling a successful connection to the GIS.
 */
public class GKConnectOK extends AbstractMessage {
    private EntityListComponent<RescueEntityType, RescueEntity> world;

    /**
       A GKConnectOK with no entities.
     */
    public GKConnectOK() {
        super("GK_CONNECT_OK", MessageConstants.GK_CONNECT_OK);
        world = new EntityListComponent<RescueEntityType, RescueEntity>("Entities", RescueEntityFactory.INSTANCE);
        addMessageComponent(world);
    }

    /**
       A GKConnectOK with a specified entity list.
       @param entities The entities to send.
     */
    public GKConnectOK(List<RescueEntity> entities) {
        this();
        world.setEntities(entities);
    }

    /**
       Get the entity list.
       @return All entities.
     */
    public List<RescueEntity> getEntities() {
        return world.getEntities();
    }
}