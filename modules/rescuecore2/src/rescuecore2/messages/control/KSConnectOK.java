package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rescuecore2.config.Config;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.ConfigProto;
import rescuecore2.messages.control.ControlMessageProto.EntityProto;
import rescuecore2.messages.control.ControlMessageProto.KSConnectOKProto;
import rescuecore2.messages.control.ControlMessageProto.PropertyProto;
import rescuecore2.registry.Registry;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * A message for signalling a successful connection to the kernel.
 */
public class KSConnectOK extends AbstractMessage {

  private int simID;
  private int requestID;
  private List<Entity> world;
  private Config config;

  /**
   * A KSConnectOK message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KSConnectOK(InputStream in) throws IOException {
    super(ControlMessageURN.KS_CONNECT_OK.toString());
    this.read(in);
  }

  /**
   * A populated KSConnectOK message.
   *
   * @param simID       The ID of the simulator that has successfully connected.
   * @param requestID   The request ID.
   * @param allEntities All Entities in the world.
   * @param config      The Config that the simulator knows about.
   */
  public KSConnectOK(int simID, int requestID, Collection<? extends Entity> allEntities, Config config) {
    super(ControlMessageURN.KS_CONNECT_OK.toString());

    this.simID = simID;
    this.requestID = requestID;
    this.world = new ArrayList<Entity>();
    this.world.addAll(allEntities);
    this.config = config;
  }

  /**
   * Get the simulator ID for this message.
   *
   * @return The simulator ID.
   */
  public int getSimulatorID() {
    return this.simID;
  }

  /**
   * Get the request ID.
   *
   * @return The request ID.
   */
  public int getRequestID() {
    return this.requestID;
  }

  /**
   * Get the entity list.
   *
   * @return All entities in the world.
   */
  public Collection<Entity> getEntities() {
    return this.world;
  }

  /**
   * Get the Config.
   *
   * @return The simulator config.
   */
  public Config getConfig() {
    return this.config;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    KSConnectOKProto.Builder ksConnectOKBuilder = KSConnectOKProto.newBuilder().setSimID(this.simID)
        .setRequestID(this.requestID);

    for (Entity entity : this.world) {
      EntityProto.Builder entityProtoBuilder = EntityProto.newBuilder()
          .setUrnID(StandardEntityURN.fromString(entity.getURN()).ordinal()).setEntityID(entity.getID().getValue());

      for (Property<?> property : entity.getProperties()) {
        if (property.isDefined()) {
          PropertyProto propertyProto = MsgProtoBuf.setPropertyProto(property);
          entityProtoBuilder.addProperties(propertyProto);
        }
      }
      ksConnectOKBuilder.addEntities(entityProtoBuilder.build());
    }

    ConfigProto configProto = MsgProtoBuf.setConfigProto(this.config);
    ksConnectOKBuilder.setConfig(configProto);

    KSConnectOKProto ksConnectOK = ksConnectOKBuilder.build();
    ksConnectOK.writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    KSConnectOKProto ksConnectOK = KSConnectOKProto.parseFrom(in);

    this.simID = ksConnectOK.getSimID();
    this.requestID = ksConnectOK.getRequestID();

    this.world = new ArrayList<Entity>();

    List<EntityProto> entityList = ksConnectOK.getEntitiesList();
    for (EntityProto entityProto : entityList) {
      String entityURN = StandardEntityURN.formInt(entityProto.getUrnID()).toString();
      int entityID = entityProto.getEntityID();

      Entity entity = Registry.getCurrentRegistry().createEntity(entityURN, new EntityID(entityID));

      if (entity != null) {
        Map<String, List<Object>> properties = new HashMap<String, List<Object>>();
        for (PropertyProto propertyProto : entityProto.getPropertiesList()) {
          String propertyURN = StandardPropertyURN.fromInt(propertyProto.getUrnID()).toString();

          List<Object> property = MsgProtoBuf.setPropertyFields(propertyProto);

          properties.put(propertyURN, property);
        }

        entity.setEntity(properties);

        this.world.add(entity);
      }
    }

    ConfigProto configProto = ksConnectOK.getConfig();
    this.config = MsgProtoBuf.setConfig(configProto);
  }
}