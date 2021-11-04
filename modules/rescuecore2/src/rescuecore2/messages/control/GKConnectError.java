package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.StringComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
 * A message for signalling an unsuccessful connection to the GIS.
 */
public class GKConnectError extends AbstractMessage {
  private StringComponent reason;

  /**
   * A GKConnectError message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public GKConnectError(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * A GKConnectError with a specified reason.
   *
   * @param reason The reason for the error.
   */
  public GKConnectError(String reason) {
    this();
    this.reason.setValue(reason);
  }

  private GKConnectError() {
    super(ControlMessageURN.GK_CONNECT_ERROR);
    reason = new StringComponent(ControlMessageComponentURN.Reason);
    addMessageComponent(reason);
  }

  public GKConnectError(MessageProto proto) {
    this();
    fromMessageProto(proto);
  }

  /**
   * Get the reason for the error.
   *
   * @return The reason for the error.
   */
  public String getReason() {
    return reason.getValue();
  }
}
