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
import rescuecore2.messages.control.ControlMessageProto.KAConnectOKProto;
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
public class KAConnectOK extends AbstractMessage {

  private int requestID;
  private EntityID agentID;
  private Collection<Entity> world;
  private Config config;

  /**
   * A KAConnectOK message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KAConnectOK(InputStream in) throws IOException {
    super(ControlMessageURN.KA_CONNECT_OK.toString());
    this.read(in);
  }

  /**
   * A populated KAConnectOK message.
   *
   * @param requestID   The request ID.
   * @param agentID     The ID of the Entity that the agent will be controlling.
   * @param allEntities All Entities that the agent knows about, including the
   *                    controlled object.
   * @param config      The Config that the agent knows about.
   */
  public KAConnectOK(int requestID, EntityID agentID, Collection<? extends Entity> allEntities, Config config) {
    super(ControlMessageURN.KA_CONNECT_OK.toString());
    this.requestID = requestID;
    this.agentID = agentID;
    this.world = new ArrayList<Entity>();
    this.world.addAll(allEntities);
    this.config = config;
  }

  /**
   * Get the requestID for this message.
   *
   * @return The request ID.
   */
  public int getRequestID() {
    return this.requestID;
  }

  /**
   * Get the ID of the agent-controlled object.
   *
   * @return The agent ID.
   */
  public EntityID getAgentID() {
    return this.agentID;
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
   * @return The agent config.
   */
  public Config getConfig() {
    return this.config;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    KAConnectOKProto.Builder kaConnectOKBuilder = KAConnectOKProto.newBuilder().setRequestID(this.requestID)
        .setAgentID(this.agentID.getValue());

    for (Entity entity : this.world) {
      EntityProto.Builder entityProtoBuilder = EntityProto.newBuilder()
          .setUrnID(StandardEntityURN.fromString(entity.getURN()).ordinal()).setEntityID(entity.getID().getValue());

      for (Property<?> property : entity.getProperties()) {
        if (property.isDefined()) {
          PropertyProto propertyProto = MsgProtoBuf.setPropertyProto(property);
          entityProtoBuilder.addProperties(propertyProto);
        }
      }
      kaConnectOKBuilder.addEntities(entityProtoBuilder.build());
    }

    ConfigProto configProto = MsgProtoBuf.setConfigProto(this.config);
    kaConnectOKBuilder.setConfig(configProto);

    KAConnectOKProto kaConnectOK = kaConnectOKBuilder.build();
    kaConnectOK.writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    KAConnectOKProto kaConnectOK = KAConnectOKProto.parseFrom(in);

    this.requestID = kaConnectOK.getRequestID();
    this.agentID = new EntityID(kaConnectOK.getAgentID());

    this.world = new ArrayList<Entity>();

    List<EntityProto> entityList = kaConnectOK.getEntitiesList();
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

    ConfigProto configProto = kaConnectOK.getConfig();
    this.config = MsgProtoBuf.setConfig(configProto);
  }
}