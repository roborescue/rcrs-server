package rescuecore2.messages.control;

import java.util.List;
import java.util.Collection;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.EntityListComponent;
import rescuecore2.worldmodel.Entity;

/**
   A message for signalling a successful connection to the GIS.
 */
public class GKConnectOK extends AbstractMessage implements Control {
    private EntityListComponent world;

    /**
       A GKConnectOK with no entities.
     */
    public GKConnectOK() {
        super("GK_CONNECT_OK", ControlMessageConstants.GK_CONNECT_OK);
        world = new EntityListComponent("Entities");
        addMessageComponent(world);
    }

    /**
       A GKConnectOK with a specified entity list.
       @param entities The entities to send.
     */
    public GKConnectOK(Collection<? extends Entity> entities) {
        this();
        world.setEntities(entities);
    }

    /**
       Get the entity list.
       @return All entities.
     */
    public List<Entity> getEntities() {
        return world.getEntities();
    }
}