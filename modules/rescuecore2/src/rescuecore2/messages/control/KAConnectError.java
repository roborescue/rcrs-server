package rescuecore2.messages.control;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import rescuecore2.messages.control.ControlMessageProto.KAConnectErrorProto;
import rescuecore2.messages.AbstractMessage;

/**
 * A message for signalling an unsuccessful connection to the kernel.
 */
public class KAConnectError extends AbstractMessage {

  private int    requestID;
  private String reason;


  /**
   * A KAConnectError message that populates its data from a stream.
   *
   * @param in
   *          The InputStream to read.
   * @throws IOException
   *           If there is a problem reading the stream.
   */
  public KAConnectError( InputStream in ) throws IOException {
    super( ControlMessageURN.KA_CONNECT_ERROR.toString() );
    this.read( in );
  }


  /**
   * A KAConnectError with specified request ID and reason.
   *
   * @param id
   *          The ID of the request that failed.
   * @param message
   *          The reason for the error.
   */
  public KAConnectError( int id, String message ) {
    super( ControlMessageURN.KA_CONNECT_ERROR.toString() );
    this.requestID = id;
    this.reason = message;
  }


  /**
   * Get the request ID for the message.
   *
   * @return The request ID for the message.
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
    KAConnectErrorProto kaConnectError = KAConnectErrorProto.newBuilder()
        .setRequestID( this.requestID ).setReason( this.reason ).build();

    kaConnectError.writeTo( out );
  }


  @Override
  public void read( InputStream in ) throws IOException {
    KAConnectErrorProto kaConnectError = KAConnectErrorProto.parseFrom( in );

    this.requestID = kaConnectError.getRequestID();
    this.reason = kaConnectError.getReason();
  }
}