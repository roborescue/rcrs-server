package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.EntityIDRequestProto;

/**
 * A message from a simulator requesting a new EntityID.
 */
public class EntityIDRequest extends AbstractMessage {

  private int simID;
  private int requestID;
  private int count;

  /**
   * Construct an EntityIDRequest message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public EntityIDRequest(InputStream in) throws IOException {
    super(ControlMessageURN.ENTITY_ID_REQUEST.toString());
    this.read(in);
  }

  /**
   * Construct an EntityIDRequest message.
   *
   * @param simID     The ID of the simulator making the request.
   * @param requestID A unique ID number for this request.
   * @param number    The number of IDs requested.
   */
  public EntityIDRequest(int simID, int requestID, int number) {
    super(ControlMessageURN.ENTITY_ID_REQUEST.toString());
    this.simID = simID;
    this.requestID = requestID;
    this.count = number;
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
   * Get the number of IDs requested.
   *
   * @return The number of IDs requested.
   */
  public int getCount() {
    return this.count;
  }

  public void write(OutputStream out) throws IOException {
    EntityIDRequestProto entityIDRequest = EntityIDRequestProto.newBuilder().setSimID(this.simID)
        .setRequestID(this.requestID).setCount(this.count).build();

    entityIDRequest.writeTo(out);
  }

  public void read(InputStream in) throws IOException {
    EntityIDRequestProto entityIDRequest = EntityIDRequestProto.parseFrom(in);

    this.simID = entityIDRequest.getSimID();
    this.requestID = entityIDRequest.getRequestID();
    this.count = entityIDRequest.getCount();
  }
}