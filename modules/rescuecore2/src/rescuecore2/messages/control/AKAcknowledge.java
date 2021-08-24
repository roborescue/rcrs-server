package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.AKAcknowledgeProto;
import rescuecore2.worldmodel.EntityID;

/**
 * A message for acknowledging a connection to the kernel.
 */
public class AKAcknowledge extends AbstractMessage {

  private int requestID;
  private EntityID agentID;

  /**
   * An AKAcknowledge message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public AKAcknowledge(InputStream in) throws IOException {
    super(ControlMessageURN.AK_ACKNOWLEDGE.toString());
    this.read(in);
  }

  /**
   * AKAcknowledge message with specific request ID and agent ID components.
   *
   * @param requestID The request ID.
   * @param agentID   The agent ID.
   */
  public AKAcknowledge(int requestID, EntityID agentID) {
    super(ControlMessageURN.AK_ACKNOWLEDGE.toString());
    this.requestID = requestID;
    this.agentID = agentID;
  }

  /**
   * Get the request ID of this acknowledgement.
   *
   * @return The request ID component.
   */
  public int getRequestID() {
    return this.requestID;
  }

  /**
   * Get the agent ID of this acknowledgement.
   *
   * @return The agent ID component.
   */
  public EntityID getAgentID() {
    return this.agentID;
  }

  public void write(OutputStream out) throws IOException {
    AKAcknowledgeProto akAcknowledge = AKAcknowledgeProto.newBuilder().setRequestID(this.requestID)
        .setAgentID(this.agentID.getValue()).build();

    akAcknowledge.writeTo(out);
  }

  public void read(InputStream in) throws IOException {
    AKAcknowledgeProto akAcknowledge = AKAcknowledgeProto.parseFrom(in);

    this.requestID = akAcknowledge.getRequestID();
    this.agentID = new EntityID(akAcknowledge.getAgentID());
  }
}