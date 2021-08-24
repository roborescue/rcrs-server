package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.KVConnectErrorProto;

/**
 * A message for signalling an unsuccessful connection to the kernel.
 */
public class KVConnectError extends AbstractMessage {

  private int requestID;
  private String reason;

  /**
   * A KVConnectError message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KVConnectError(InputStream in) throws IOException {
    super(ControlMessageURN.KV_CONNECT_ERROR.toString());
    this.read(in);
  }

  /**
   * A KVConnectError with specified request ID and reason.
   *
   * @param requestID The request ID.
   * @param message   The reason for the error.
   */
  public KVConnectError(int requestID, String message) {
    super(ControlMessageURN.KV_CONNECT_ERROR.toString());
    this.requestID = requestID;
    this.reason = message;
  }

  /**
   * Get the request ID.
   *
   * @return The request ID.
   */
  public int getRequestID() {
    return this.requestID;
  }

  /**
   * Get the reason for the error.
   *
   * @return The reason for the error.
   */
  public String getReason() {
    return this.reason;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    KVConnectErrorProto kvConnectError = KVConnectErrorProto.newBuilder().setRequestID(this.requestID)
        .setReason(this.reason).build();

    kvConnectError.writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    KVConnectErrorProto kvConnectError = KVConnectErrorProto.parseFrom(in);

    this.requestID = kvConnectError.getRequestID();
    this.reason = kvConnectError.getReason();
  }
}