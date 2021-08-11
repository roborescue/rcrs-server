package rescuecore2.messages.control;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.KGConnectProto;

/**
 * A message for connecting to the GIS.
 */
public class KGConnect extends AbstractMessage {

  private int version;


  /**
   * A KGConnect message that populates its data from a stream.
   *
   * @param in
   *          The InputStream to read.
   * @throws IOException
   *           If there is a problem reading the stream.
   */
  public KGConnect( InputStream in ) throws IOException {
    super( ControlMessageURN.KG_CONNECT.toString() );
    this.read( in );
  }


  /**
   * A KGConnect message with a specified version number.
   *
   * @param version
   *          The version number field.
   */
  public KGConnect( int version ) {
    super( ControlMessageURN.KG_CONNECT.toString() );
    this.version = version;
  }


  /**
   * Get the version number of the message.
   *
   * @return The version number field.
   */
  public int getVersion() {
    return this.version;
  }


  @Override
  public void write( OutputStream out ) throws IOException {
    KGConnectProto kgConnect = KGConnectProto.newBuilder()
        .setVersion( this.version ).build();

    kgConnect.writeTo( out );
  }


  @Override
  public void read( InputStream in ) throws IOException {
    KGConnectProto kgConnect = KGConnectProto.parseFrom( in );

    this.version = kgConnect.getVersion();
  }
}
