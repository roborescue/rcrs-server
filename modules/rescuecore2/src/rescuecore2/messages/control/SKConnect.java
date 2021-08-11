package rescuecore2.messages.control;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.SKConnectProto;

/**
 * A message for connecting a simulator to the kernel.
 */
public class SKConnect extends AbstractMessage {

  private int    requestID;
  private int    version;
  private String simulatorName;


  /**
   * An SKConnect message that populates its data from a stream.
   *
   * @param in
   *          The InputStream to read.
   * @throws IOException
   *           If there is a problem reading the stream.
   */
  public SKConnect( InputStream in ) throws IOException {
    super( ControlMessageURN.SK_CONNECT.toString() );
    this.read( in );
  }


  /**
   * An SKConnect with a given version and request ID.
   *
   * @param requestID
   *          The request ID.
   * @param version
   *          The version number.
   * @param name
   *          The name of the simulator.
   */
  public SKConnect( int requestID, int version, String name ) {
    super( ControlMessageURN.SK_CONNECT.toString() );
    this.requestID = requestID;
    this.version = version;
    this.simulatorName = name;
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
   * Get the version number of this request.
   *
   * @return The version number.
   */
  public int getVersion() {
    return this.version;
  }


  /**
   * Get the simulator name.
   *
   * @return The name of the simulator.
   */
  public String getSimulatorName() {
    return this.simulatorName;
  }


  public void write( OutputStream out ) throws IOException {
    SKConnectProto skConnect = SKConnectProto.newBuilder()
        .setRequestID( this.requestID ).setVersion( this.version )
        .setSimulatorName( this.simulatorName ).build();

    skConnect.writeTo( out );
  }


  public void read( InputStream in ) throws IOException {
    SKConnectProto skConnect = SKConnectProto.parseFrom( in );

    this.requestID = skConnect.getRequestID();
    this.version = skConnect.getVersion();
    this.simulatorName = skConnect.getSimulatorName();
  }
}