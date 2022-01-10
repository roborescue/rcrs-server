package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
 * A message from a simulator requesting a new EntityID.
 */
public class EntityIDRequest extends AbstractMessage {
  private IntComponent simID;
  private IntComponent requestID;
  private IntComponent count;

  /**
   * Construct an EntityIDRequest message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public EntityIDRequest(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * Construct an EntityIDRequest message.
   *
   * @param simID     The ID of the simulator making the request.
   * @param requestID A unique ID number for this request.
   * @param number    The number of IDs requested.
   */
  public EntityIDRequest(int simID, int requestID, int number) {
    this();
    this.simID.setValue(simID);
    this.requestID.setValue(requestID);
    this.count.setValue(number);
  }

  private EntityIDRequest() {
    super(ControlMessageURN.ENTITY_ID_REQUEST);
    simID = new IntComponent(ControlMessageComponentURN.SimulatorID);
    requestID = new IntComponent(ControlMessageComponentURN.RequestNumber);
    count = new IntComponent(ControlMessageComponentURN.NumberOfIDs);
    addMessageComponent(simID);
    addMessageComponent(requestID);
    addMessageComponent(count);
  }

  public EntityIDRequest(MessageProto proto) {
    this();
    fromMessageProto(proto);
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

  /**
   * Get the number of IDs requested.
   *
   * @return The number of IDs requested.
   */
  public int getCount() {
    return count.getValue();
  }
}
