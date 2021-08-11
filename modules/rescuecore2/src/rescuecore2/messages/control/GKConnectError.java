package rescuecore2.messages.control;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.GKConnectErrorProto;

/**
 * A message for signalling an unsuccessful connection to the GIS.
 */
public class GKConnectError extends AbstractMessage {

  private String reason;


  /**
   * A GKConnectError message that populates its data from a stream.
   *
   * @param in
   *          The InputStream to read.
   * @throws IOException
   *           If there is a problem reading the stream.
   */
  public GKConnectError( InputStream in ) throws IOException {
    super( ControlMessageURN.GK_CONNECT_ERROR.toString() );
    this.read( in );
  }


  /**
   * A GKConnectError with a specified reason.
   *
   * @param reason
   *          The reason for the error.
   */
  public GKConnectError( String reason ) {
    super( ControlMessageURN.GK_CONNECT_ERROR.toString() );
    this.reason = reason;
  }


  /**
   * Get the reason for the error.
   *
   * @return The reason for the error.
   */
  public String getReason() {
    return this.reason;
  }


  public void write( OutputStream out ) throws IOException {
    GKConnectErrorProto gkConnectError = GKConnectErrorProto.newBuilder()
        .setReason( this.reason ).build();

    gkConnectError.writeTo( out );
  }


  public void read( InputStream in ) throws IOException {
    GKConnectErrorProto gkConnectError = GKConnectErrorProto.parseFrom( in );

    this.reason = gkConnectError.getReason();
  }
}