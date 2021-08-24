package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.ShutdownProto;

/**
 * A message from the kernel indicating that components should shut down.
 */
public class Shutdown extends AbstractMessage {

  /**
   * Construct a Shutdown message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public Shutdown(InputStream in) throws IOException {
    super(ControlMessageURN.SHUTDOWN.toString());
    this.read(in);
  }

  /**
   * Construct a Shutdown message.
   */
  public Shutdown() {
    super(ControlMessageURN.SHUTDOWN.toString());
  }

  @Override
  public void write(OutputStream out) throws IOException {
    ShutdownProto akAcknowledge = ShutdownProto.newBuilder().build();
    akAcknowledge.writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    ShutdownProto.parseFrom(in);
  }
}