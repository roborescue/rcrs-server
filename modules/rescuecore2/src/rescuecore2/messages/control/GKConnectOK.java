package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.EntityProto;
import rescuecore2.messages.control.ControlMessageProto.GKConnectOKProto;
import rescuecore2.messages.control.ControlMessageProto.PropertyProto;
import rescuecore2.registry.Registry;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * A message for signalling a successful connection to the GIS.
 */
public class GKConnectOK extends AbstractMessage {

  private List<Entity> world;

  /**
   * A GKConnectOK message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public GKConnectOK(InputStream in) throws IOException {
    super(ControlMessageURN.GK_CONNECT_OK.toString());
    this.read(in);
  }

  /**
   * A GKConnectOK with a specified entity list.
   *
   * @param entities The entities to send.
   */
  public GKConnectOK(Collection<? extends Entity> entities) {
    super(ControlMessageURN.GK_CONNECT_OK.toString());
    this.world = new ArrayList<>(entities);
  }

  /**
   * Get the entity list.
   *
   * @return All entities.
   */
  public List<Entity> getEntities() {
    return this.world;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    GKConnectOKProto.Builder gkConnectOKBuilder = GKConnectOKProto.newBuilder();

    for (Entity entity : this.world) {
      gkConnectOKBuilder.addEntities(MsgProtoBuf.setEntityProto(entity));
    }

    GKConnectOKProto gkConnectOK = gkConnectOKBuilder.build();
    gkConnectOK.writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    GKConnectOKProto gkConnectOK = GKConnectOKProto.parseFrom(in);

    this.world = new ArrayList<Entity>();

    List<EntityProto> entityList = gkConnectOK.getEntitiesList();
    for (EntityProto entityProto : entityList) {
    	Entity entity = MsgProtoBuf.setEntity(entityProto);
        if (entity != null) {
          this.world.add(entity);
        } 
    }
  }
}