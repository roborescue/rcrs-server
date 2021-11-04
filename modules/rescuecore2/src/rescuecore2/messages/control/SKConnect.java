package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.components.StringComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
 * A message for connecting a simulator to the kernel.
 */
public class SKConnect extends AbstractMessage {
  private IntComponent requestID;
  private IntComponent version;
  private StringComponent simulatorName;

  /**
   * An SKConnect message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public SKConnect(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * An SKConnect with a given version and request ID.
   *
   * @param requestID The request ID.
   * @param version   The version number.
   * @param name      The name of the simulator.
   */
  public SKConnect(int requestID, int version, String name) {
    this();
    this.requestID.setValue(requestID);
    this.version.setValue(version);
    this.simulatorName.setValue(name);
  }

  private SKConnect() {
    super(ControlMessageURN.SK_CONNECT);
    requestID = new IntComponent(ControlMessageComponentURN.RequestID);
    version = new IntComponent(ControlMessageComponentURN.Version);
    simulatorName = new StringComponent(ControlMessageComponentURN.Name);
    addMessageComponent(requestID);
    addMessageComponent(version);
    addMessageComponent(simulatorName);
  }

  public SKConnect(MessageProto proto) {
    this();
    fromMessageProto(proto);
  }

  /**
   * Get the version number of this request.
   *
   * @return The version number.
   */
  public int getVersion() {
    return version.getValue();
  }

  /**
   * Get the request ID.
   *
   * @return The request ID.
   */
  public int getRequestID() {
    return requestID.getValue();
  }

  /**
   * Get the simulator name.
   *
   * @return The name of the simulator.
   */
  public String getSimulatorName() {
    return simulatorName.getValue();
  }
}
