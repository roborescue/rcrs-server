package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.EntityIDResponseProto;
import rescuecore2.worldmodel.EntityID;

/**
 * A message from a the kernel supplying a new EntityID.
 */
public class EntityIDResponse extends AbstractMessage {

  private int simID;
  private int requestID;
  private List<EntityID> newID;

  /**
   * Construct an EntityIDResponse message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public EntityIDResponse(InputStream in) throws IOException {
    super(ControlMessageURN.ENTITY_ID_RESPONSE.toString());
    this.read(in);
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
    super(ControlMessageURN.ENTITY_ID_RESPONSE.toString());
    this.simID = simID;
    this.requestID = requestID;
    this.newID = ids;
  }

  /**
   * Get the ID of the simulator making the request.
   *
   * @return The simulator ID.
   */
  public int getSimulatorID() {
    return this.simID;
  }

  /**
   * Get the ID of this request.
   *
   * @return The request ID.
   */
  public int getRequestID() {
    return this.requestID;
  }

  /**
   * Get the new entity IDs.
   *
   * @return The new entity IDs.
   */
  public List<EntityID> getEntityIDs() {
    return this.newID;
  }

  public void write(OutputStream out) throws IOException {
    EntityIDResponseProto.Builder entityIDResponseBuilder = EntityIDResponseProto.newBuilder().setSimID(this.simID)
        .setRequestID(this.requestID);

    for (EntityID newID : this.newID) {
      entityIDResponseBuilder.addNewIDs(newID.getValue());
    }

    EntityIDResponseProto entityIDResponse = entityIDResponseBuilder.build();
    entityIDResponse.writeTo(out);
  }

  public void read(InputStream in) throws IOException {
    EntityIDResponseProto entityIDResponse = EntityIDResponseProto.parseFrom(in);

    this.simID = entityIDResponse.getSimID();
    this.requestID = entityIDResponse.getRequestID();

    this.newID = new ArrayList<EntityID>();
    for (Integer newID : entityIDResponse.getNewIDsList()) {
      this.newID.add(new EntityID(newID));
    }
  }
}