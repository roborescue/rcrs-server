package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
 * A message for connecting to the GIS.
 */
public class KGConnect extends AbstractMessage {
  private IntComponent version;

  /**
   * A KGConnect message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KGConnect(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * A KGConnect message with a specified version number.
   *
   * @param version The version number field.
   */
  public KGConnect(int version) {
    this();
    this.version.setValue(version);
  }

  private KGConnect() {
    super(ControlMessageURN.KG_CONNECT);
    version = new IntComponent(ControlMessageComponentURN.Version, 0);
    addMessageComponent(version);
  }

  public KGConnect(MessageProto proto) {
    this();
    fromMessageProto(proto);
  }

  /**
   * Get the version number of the message.
   *
   * @return The version number field.
   */
  public int getVersion() {
    return version.getValue();
  }
}
