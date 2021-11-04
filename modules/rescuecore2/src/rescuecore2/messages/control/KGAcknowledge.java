package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
 * A message for acknowleding a connection to the GIS.
 */
public class KGAcknowledge extends AbstractMessage {
  /**
   * A KGAcknowledge message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KGAcknowledge(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * A KGAcknowldge message.
   */
  public KGAcknowledge() {
    super(ControlMessageURN.KG_ACKNOWLEDGE);
  }

  public KGAcknowledge(MessageProto proto) {
    this();
    fromMessageProto(proto);
  }
}
