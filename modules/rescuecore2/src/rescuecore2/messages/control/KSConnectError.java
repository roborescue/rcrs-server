package rescuecore2.messages.control;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.KSConnectErrorProto;

/**
 * A message for signalling an unsuccessful connection to the kernel.
 */
public class KSConnectError extends AbstractMessage {

  private int    requestID;
  private String reason;


  /**
   * A KSConnectError message that populates its data from a stream.
   *
   * @param in
   *          The InputStream to read.
   * @throws IOException
   *           If there is a problem reading the stream.
   */
  public KSConnectError( InputStream in ) throws IOException {
    super( ControlMessageURN.KS_CONNECT_ERROR.toString() );
    this.read( in );
  }


  /**
   * A KSConnectError with specified request ID and reason.
   *
   * @param requestID
   *          The request ID.
   * @param message
   *          The reason for the error.
   */
  public KSConnectError( int requestID, String message ) {
    super( ControlMessageURN.KS_CONNECT_ERROR.toString() );
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
  public void write( OutputStream out ) throws IOException {
    KSConnectErrorProto ksConnectError = KSConnectErrorProto.newBuilder()
        .setRequestID( this.requestID ).setReason( this.reason ).build();

    ksConnectError.writeTo( out );
  }


  @Override
  public void read( InputStream in ) throws IOException {
    KSConnectErrorProto ksConnectError = KSConnectErrorProto.parseFrom( in );

    this.requestID = ksConnectError.getRequestID();
    this.reason = ksConnectError.getReason();
  }
}