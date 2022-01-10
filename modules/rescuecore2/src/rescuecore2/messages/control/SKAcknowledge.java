package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
 * A message for acknowledging a connection to the kernel.
 */
public class SKAcknowledge extends AbstractMessage {
  private IntComponent requestID;
  private IntComponent simulatorID;

  /**
   * An SKAcknowledge message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public SKAcknowledge(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * SKAcknowledge message with specific request ID and simulator ID components.
   *
   * @param requestID   The value of the request ID component.
   * @param simulatorID The value of the simulator ID component.
   */
  public SKAcknowledge(int requestID, int simulatorID) {
    this();
    this.requestID.setValue(requestID);
    this.simulatorID.setValue(simulatorID);
  }

  private SKAcknowledge() {
    super(ControlMessageURN.SK_ACKNOWLEDGE);
    requestID = new IntComponent(ControlMessageComponentURN.RequestID);
    simulatorID = new IntComponent(ControlMessageComponentURN.SimulatorID);
    addMessageComponent(requestID);
    addMessageComponent(simulatorID);
  }

  public SKAcknowledge(MessageProto proto) {
    this();
    fromMessageProto(proto);
  }

  /**
   * Get the request ID.
   *
   * @return The request ID component.
   */
  public int getRequestID() {
    return requestID.getValue();
  }

  /**
   * Get the simulator ID.
   *
   * @return The simulator ID component.
   */
  public int getSimulatorID() {
    return simulatorID.getValue();
  }
}
