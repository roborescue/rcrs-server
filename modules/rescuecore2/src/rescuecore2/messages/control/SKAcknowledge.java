package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.SKAcknowledgeProto;

/**
 * A message for acknowledging a connection to the kernel.
 */
public class SKAcknowledge extends AbstractMessage {

  private int requestID;
  private int simID;

  /**
   * An SKAcknowledge message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public SKAcknowledge(InputStream in) throws IOException {
    super(ControlMessageURN.SK_ACKNOWLEDGE.toString());
    this.read(in);
  }

  /**
   * SKAcknowledge message with specific request ID and simulator ID components.
   *
   * @param requestID   The value of the request ID component.
   * @param simulatorID The value of the simulator ID component.
   */
  public SKAcknowledge(int requestID, int simulatorID) {
    super(ControlMessageURN.SK_ACKNOWLEDGE.toString());
    this.simID = simulatorID;
    this.requestID = requestID;
  }

  /**
   * Get the simulator ID.
   *
   * @return The simulator ID component.
   */
  public int getSimulatorID() {
    return this.simID;
  }

  /**
   * Get the request ID.
   *
   * @return The request ID component.
   */
  public int getRequestID() {
    return this.requestID;
  }

  public void write(OutputStream out) throws IOException {
    SKAcknowledgeProto skAcknowledge = SKAcknowledgeProto.newBuilder().setSimID(this.simID).setRequestID(this.requestID)
        .build();

    skAcknowledge.writeTo(out);
  }

  public void read(InputStream in) throws IOException {
    SKAcknowledgeProto skAcknowledge = SKAcknowledgeProto.parseFrom(in);

    this.simID = skAcknowledge.getSimID();
    this.requestID = skAcknowledge.getRequestID();
  }
}