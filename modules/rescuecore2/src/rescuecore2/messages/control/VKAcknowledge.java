package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
 * A message for acknowledging a connection to the kernel.
 */
public class VKAcknowledge extends AbstractMessage {
  private IntComponent requestID;
  private IntComponent viewerID;

  /**
   * A VKAcknowledge message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public VKAcknowledge(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * VKAcknowledge message with specific request ID and viewer ID components.
   *
   * @param requestID The value of the request ID component.
   * @param viewerID  The value of the viewer ID component.
   */
  public VKAcknowledge(int requestID, int viewerID) {
    this();
    this.requestID.setValue(requestID);
    this.viewerID.setValue(viewerID);
  }

  private VKAcknowledge() {
    super(ControlMessageURN.VK_ACKNOWLEDGE);
    requestID = new IntComponent(ControlMessageComponentURN.RequestID);
    viewerID = new IntComponent(ControlMessageComponentURN.ViewerID);
    addMessageComponent(requestID);
    addMessageComponent(viewerID);
  }

  public VKAcknowledge(MessageProto proto) {
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
   * Get the viewer ID.
   *
   * @return The viewer ID component.
   */
  public int getViewerID() {
    return viewerID.getValue();
  }
}
