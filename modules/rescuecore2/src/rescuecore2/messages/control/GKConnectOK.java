package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.EntityListComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.Entity;

/**
 * A message for signalling a successful connection to the GIS.
 */
public class GKConnectOK extends AbstractMessage {
  private EntityListComponent world;

  /**
   * A GKConnectOK message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public GKConnectOK(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * A GKConnectOK with a specified entity list.
   *
   * @param entities The entities to send.
   */
  public GKConnectOK(Collection<? extends Entity> entities) {
    this();
    world.setEntities(entities);
  }

  private GKConnectOK() {
    super(ControlMessageURN.GK_CONNECT_OK);
    world = new EntityListComponent(ControlMessageComponentURN.Entities);
    addMessageComponent(world);
  }

  public GKConnectOK(MessageProto proto) {
    this();
    fromMessageProto(proto);
  }

  /**
   * Get the entity list.
   *
   * @return All entities.
   */
  public List<Entity> getEntities() {
    return world.getEntities();
  }
}
