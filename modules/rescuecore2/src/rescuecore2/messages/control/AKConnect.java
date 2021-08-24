package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.AKConnectProto;

/**
 * A message for connecting an agent to the kernel.
 */
public class AKConnect extends AbstractMessage {

  private int requestID;
  private int version;
  private String agentName;
  private List<String> requestedEntityTypes;

  /**
   * An AKConnect message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public AKConnect(InputStream in) throws IOException {
    super(ControlMessageURN.AK_CONNECT.toString());
    this.read(in);
  }

  /**
   * An AKConnect with particular version, requestID and requested entity types.
   *
   * @param requestID            The request ID.
   * @param version              The version number.
   * @param agentName            The name of the agent.
   * @param requestedEntityTypes The set of requested entity types.
   */
  public AKConnect(int requestID, int version, String agentName, String... requestedEntityTypes) {
    super(ControlMessageURN.AK_CONNECT.toString());
    this.requestID = requestID;
    this.version = version;
    this.agentName = agentName;
    this.requestedEntityTypes = Arrays.asList(requestedEntityTypes);
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
   * Get the name of the agent making this request.
   *
   * @return The agent name.
   */
  public String getAgentName() {
    return this.agentName;
  }

  /**
   * Get the requested entity types.
   *
   * @return The requested entity types.
   */
  public List<String> getRequestedEntityTypes() {
    return this.requestedEntityTypes;
  }

  public void write(OutputStream out) throws IOException {
    AKConnectProto akConnect = AKConnectProto.newBuilder().setRequestID(this.requestID).setVersion(this.version)
        .setAgentName(this.agentName).addAllRequestedEntityTypes(this.requestedEntityTypes).build();

    akConnect.writeTo(out);
  }

  public void read(InputStream in) throws IOException {
    AKConnectProto akConnect = AKConnectProto.parseFrom(in);

    this.requestID = akConnect.getRequestID();
    this.version = akConnect.getVersion();
    this.agentName = akConnect.getAgentName();

    this.requestedEntityTypes = akConnect.getRequestedEntityTypesList().subList(0,
        akConnect.getRequestedEntityTypesCount());
  }
}