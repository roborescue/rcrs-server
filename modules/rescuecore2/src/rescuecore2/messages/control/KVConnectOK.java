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
import rescuecore2.messages.control.ControlMessageProto.KVConnectOKProto;
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
public class KVConnectOK extends AbstractMessage {

  private int viewerID;
  private int requestID;
  private List<Entity> world;
  private Config config;

  /**
   * A KVConnectOK message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KVConnectOK(InputStream in) throws IOException {
    super(ControlMessageURN.KV_CONNECT_OK.toString());
    this.read(in);
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
    super(ControlMessageURN.KV_CONNECT_OK.toString());
    this.viewerID = viewerID;
    this.requestID = requestID;
    this.world = new ArrayList<Entity>();
    this.world.addAll(allEntities);
    this.config = config;
  }

  /**
   * Get the viewer ID.
   *
   * @return The viewer ID.
   */
  public int getViewerID() {
    return this.viewerID;
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
   * @return The viewer config.
   */
  public Config getConfig() {
    return this.config;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    KVConnectOKProto.Builder kvConnectOKBuilder = KVConnectOKProto.newBuilder().setViewerID(this.viewerID)
        .setRequestID(this.requestID);

    for (Entity entity : this.world) {
      kvConnectOKBuilder.addEntities(MsgProtoBuf.setEntityProto(entity));
    }

    ConfigProto configProto = MsgProtoBuf.setConfigProto(this.config);
    kvConnectOKBuilder.setConfig(configProto);

    KVConnectOKProto kvConnectOK = kvConnectOKBuilder.build();
    kvConnectOK.writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    KVConnectOKProto kvConnectOK = KVConnectOKProto.parseFrom(in);

    this.viewerID = kvConnectOK.getViewerID();
    this.requestID = kvConnectOK.getRequestID();

    this.world = new ArrayList<Entity>();

    List<EntityProto> entityList = kvConnectOK.getEntitiesList();
    for (EntityProto entityProto : entityList) {
    	Entity entity = MsgProtoBuf.setEntity(entityProto);
        if (entity != null) {
          this.world.add(entity);
        }
    }

    ConfigProto configProto = kvConnectOK.getConfig();
    this.config = MsgProtoBuf.setConfig(configProto);
  }
}