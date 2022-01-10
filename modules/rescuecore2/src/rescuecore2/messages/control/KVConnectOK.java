package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import rescuecore2.config.Config;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.ConfigComponent;
import rescuecore2.messages.components.EntityListComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.Entity;

/**
 * A message for signalling a successful connection to the kernel.
 */
public class KVConnectOK extends AbstractMessage {
  private IntComponent viewerID;
  private IntComponent requestID;
  private EntityListComponent world;
  private ConfigComponent config;

  /**
   * A KVConnectOK message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KVConnectOK(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * A populated KVConnectOK message.
   *
   * @param viewerID    The viewer ID.
   * @param requestID   The request ID.
   * @param allEntities All Entities in the world.
   * @param config      The Config that the agent knows about.
   */
  public KVConnectOK(int viewerID, int requestID, Collection<? extends Entity> allEntities, Config config) {
    this();
    this.viewerID.setValue(viewerID);
    this.requestID.setValue(requestID);
    this.world.setEntities(allEntities);
    this.config.setConfig(config);
  }

  private KVConnectOK() {
    super(ControlMessageURN.KV_CONNECT_OK);
    viewerID = new IntComponent(ControlMessageComponentURN.ViewerID);
    requestID = new IntComponent(ControlMessageComponentURN.RequestID);
    world = new EntityListComponent(ControlMessageComponentURN.Entities);
    config = new ConfigComponent(ControlMessageComponentURN.AgentConfig);
    addMessageComponent(requestID);
    addMessageComponent(viewerID);
    addMessageComponent(world);
    addMessageComponent(config);
  }

  public KVConnectOK(MessageProto proto) {
    this();
    fromMessageProto(proto);
  }

  /**
   * Get the viewer ID.
   *
   * @return The viewer ID.
   */
  public int getViewerID() {
    return viewerID.getValue();
  }

  /**
   * Get the request ID.
   *
   * @return The request ID.
   */
  public int getRequestID() {
    return requestID.getValue();
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
   * @return The viewer config.
   */
  public Config getConfig() {
    return config.getConfig();
  }
}
