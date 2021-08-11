package rescuecore2.messages.control;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import rescuecore2.messages.control.ControlMessageProto.AKAcknowledgeProto;
import rescuecore2.messages.AbstractMessage;

/**
 * A message for acknowledging a connection to the GIS.
 */
public class KGAcknowledge extends AbstractMessage {

  /**
   * A KGAcknowledge message that populates its data from a stream.
   *
   * @param in
   *          The InputStream to read.
   * @throws IOException
   *           If there is a problem reading the stream.
   */
  public KGAcknowledge( InputStream in ) throws IOException {
    super( ControlMessageURN.KG_ACKNOWLEDGE.toString() );
    this.read( in );
  }


  /**
   * A KGAcknowledge message.
   */
  public KGAcknowledge() {
    super( ControlMessageURN.KG_ACKNOWLEDGE.toString() );
  }


  @Override
  public void write( OutputStream out ) throws IOException {
    AKAcknowledgeProto akAcknowledge = AKAcknowledgeProto.newBuilder().build();
    akAcknowledge.writeTo( out );
  }


  @Override
  public void read( InputStream in ) throws IOException {
    AKAcknowledgeProto.parseFrom( in );
  }
}