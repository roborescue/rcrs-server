package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.VKConnectProto;

/**
 * A message for connecting a viewer to the kernel.
 */
public class VKConnect extends AbstractMessage {

  private int requestID;
  private int version;
  private String viewerName;

  /**
   * A VKConnect message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public VKConnect(InputStream in) throws IOException {
    super(ControlMessageURN.VK_CONNECT.toString());
    this.read(in);
  }

  /**
   * A VKConnect with a given version and request ID.
   *
   * @param version   The version number.
   * @param requestID The request ID.
   * @param name      The name of the simulator.
   */
  public VKConnect(int requestID, int version, String name) {
    super(ControlMessageURN.VK_CONNECT.toString());
    this.requestID = requestID;
    this.version = version;
    this.viewerName = name;
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
   * Get the viewer name.
   *
   * @return The name of the viewer.
   */
  public String getViewerName() {
    return this.viewerName;
  }

  public void write(OutputStream out) throws IOException {
    VKConnectProto vkConnect = VKConnectProto.newBuilder().setRequestID(this.requestID).setVersion(this.version)
        .setViewerName(this.viewerName).build();

    vkConnect.writeTo(out);
  }

  public void read(InputStream in) throws IOException {
    VKConnectProto vkConnect = VKConnectProto.parseFrom(in);

    this.requestID = vkConnect.getRequestID();
    this.version = vkConnect.getVersion();
    this.viewerName = vkConnect.getViewerName();
  }
}