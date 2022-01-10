package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import rescuecore2.config.Config;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.ConfigComponent;
import rescuecore2.messages.components.EntityIDComponent;
import rescuecore2.messages.components.EntityListComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
 * A message for signalling a successful connection to the kernel.
 */
public class KAConnectOK extends AbstractMessage {
  private IntComponent requestID;
  private EntityIDComponent agentID;
  private EntityListComponent world;
  private ConfigComponent config;

  /**
   * A KAConnectOK message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KAConnectOK(InputStream in) throws IOException {
    this();
    read(in);
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
    this();
    this.requestID.setValue(requestID);
    this.agentID.setValue(agentID);
    this.world.setEntities(allEntities);
    this.config.setConfig(config);
  }

  private KAConnectOK() {
    super(ControlMessageURN.KA_CONNECT_OK);
    requestID = new IntComponent(ControlMessageComponentURN.RequestID);
    agentID = new EntityIDComponent(ControlMessageComponentURN.AgentID);
    world = new EntityListComponent(ControlMessageComponentURN.Entities);
    config = new ConfigComponent(ControlMessageComponentURN.AgentConfig);
    addMessageComponent(requestID);
    addMessageComponent(agentID);
    addMessageComponent(world);
    addMessageComponent(config);
  }

  public KAConnectOK(MessageProto proto) {
    this();
    fromMessageProto(proto);
  }

  /**
   * Get the requestID for this message.
   *
   * @return The request ID.
   */
  public int getRequestID() {
    return requestID.getValue();
  }

  /**
   * Get the ID of the agent-controlled object.
   *
   * @return The agent ID.
   */
  public EntityID getAgentID() {
    return agentID.getValue();
  }

  /**
   * Get the entity list.
   *
   * @return All entities in the world.
   */
  public Collection<Entity> getEntities() {
    return world.getEntities();
  }

  /**
   * Get the Config.
   *
   * @return The agent config.
   */
  public Config getConfig() {
    return config.getConfig();
  }
}
