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
public class KSConnectOK extends AbstractMessage {
  private IntComponent simulatorID;
  private IntComponent requestID;
  private EntityListComponent world;
  private ConfigComponent config;

  /**
   * A KSConnectOK message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KSConnectOK(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * A populated KSConnectOK message.
   *
   * @param simulatorID The ID of the simulator that has successfully connected.
   * @param requestID   The request ID.
   * @param allEntities All Entities in the world.
   * @param config      The Config that the simulator knows about.
   */
  public KSConnectOK(int simulatorID, int requestID, Collection<? extends Entity> allEntities, Config config) {
    this();
    this.simulatorID.setValue(simulatorID);
    this.requestID.setValue(requestID);
    this.world.setEntities(allEntities);
    this.config.setConfig(config);
  }

  private KSConnectOK() {
    super(ControlMessageURN.KS_CONNECT_OK);
    simulatorID = new IntComponent(ControlMessageComponentURN.SimulatorID);
    requestID = new IntComponent(ControlMessageComponentURN.RequestID);
    world = new EntityListComponent(ControlMessageComponentURN.Entities);
    config = new ConfigComponent(ControlMessageComponentURN.SimulatorConfig);
    addMessageComponent(requestID);
    addMessageComponent(simulatorID);
    addMessageComponent(world);
    addMessageComponent(config);
  }

  public KSConnectOK(MessageProto proto) {
    this();
    fromMessageProto(proto);
  }

  /**
   * Get the simulator ID for this message.
   *
   * @return The simulator ID.
   */
  public int getSimulatorID() {
    return simulatorID.getValue();
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
   * @return The simulator config.
   */
  public Config getConfig() {
    return config.getConfig();
  }
}
