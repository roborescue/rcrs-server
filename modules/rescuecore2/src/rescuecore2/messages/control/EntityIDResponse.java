package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.EntityIDListComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.EntityID;

/**
 * A message from a the kernel supplying a new EntityID.
 */
public class EntityIDResponse extends AbstractMessage {
  private IntComponent simID;
  private IntComponent requestID;
  private EntityIDListComponent newID;

  /**
   * Construct an EntityIDResponse message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public EntityIDResponse(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * Construct an EntityIDResponse message.
   *
   * @param simID     The ID of the simulator making the request.
   * @param requestID A unique ID number for this request.
   * @param ids       The new EntityIDs.
   */
  public EntityIDResponse(int simID, int requestID, EntityID... ids) {
    this(simID, requestID, Arrays.asList(ids));
  }

  /**
   * Construct an EntityIDResponse message.
   *
   * @param simID     The ID of the simulator making the request.
   * @param requestID A unique ID number for this request.
   * @param ids       The new EntityIDs.
   */
  public EntityIDResponse(int simID, int requestID, List<EntityID> ids) {
    this();
    this.simID.setValue(simID);
    this.requestID.setValue(requestID);
    this.newID.setIDs(ids);
  }

  private EntityIDResponse() {
    super(ControlMessageURN.ENTITY_ID_RESPONSE);
    simID = new IntComponent(ControlMessageComponentURN.SimulatorID);
    requestID = new IntComponent(ControlMessageComponentURN.RequestNumber);
    newID = new EntityIDListComponent(ControlMessageComponentURN.NewEntityIDs);
    addMessageComponent(simID);
    addMessageComponent(requestID);
    addMessageComponent(newID);
  }

  public EntityIDResponse(MessageProto proto) {
    this();
    fromMessageProto(proto);
  }

  /**
   * Get the new entity IDs.
   *
   * @return The new entity IDs.
   */
  public List<EntityID> getEntityIDs() {
    return newID.getIDs();
  }

  /**
   * Get the ID of the simulator making the request.
   *
   * @return The simulator ID.
   */
  public int getSimulatorID() {
    return simID.getValue();
  }

  /**
   * Get the ID of this request.
   *
   * @return The request ID.
   */
  public int getRequestID() {
    return requestID.getValue();
  }
}
