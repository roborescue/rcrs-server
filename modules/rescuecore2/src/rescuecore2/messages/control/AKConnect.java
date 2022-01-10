package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.components.IntListComponent;
import rescuecore2.messages.components.StringComponent;
import rescuecore2.messages.components.StringListComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.registry.Registry;

/**
 * A message for connecting an agent to the kernel.
 */
public class AKConnect extends AbstractMessage {
  private IntComponent requestID;
  private IntComponent version;
  private StringComponent agentName;
  private StringListComponent requestedStrEntityTypes;
  private IntListComponent requestedIntEntityTypes;

  /**
   * An AKConnect message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public AKConnect(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * An AKConnect with particular version, requestID and requested entity types.
   *
   * @param requestID            The request ID.
   * @param version              The version number.
   * @param agentName            The name of the agent.
   * @param requestedEntityTypes The set of requested entity types.
   */
  public AKConnect(int requestID, int version, String agentName, int[] requestedEntityTypes) {
    this();
    this.requestID.setValue(requestID);
    this.version.setValue(version);
    this.agentName.setValue(agentName);
    this.requestedIntEntityTypes.setValues(requestedEntityTypes);

    ArrayList<String> strRequests = new ArrayList<String>();
    for (int request : this.requestedIntEntityTypes.getValues()) {
      strRequests.add(Registry.getCurrentRegistry().toURN_Str(request));
    }
    this.requestedStrEntityTypes.setValues(strRequests);
  }

  private AKConnect() {
    super(ControlMessageURN.AK_CONNECT);
    this.requestID = new IntComponent(ControlMessageComponentURN.RequestID);
    this.version = new IntComponent(ControlMessageComponentURN.Version);
    this.agentName = new StringComponent(ControlMessageComponentURN.Name);
    this.requestedIntEntityTypes = new IntListComponent(ControlMessageComponentURN.RequestedEntityTypes);
    this.requestedStrEntityTypes = new StringListComponent(ControlMessageComponentURN.RequestedEntityTypes);
    addMessageComponent(requestID);
    addMessageComponent(version);
    addMessageComponent(agentName);

    switch (this.version.getValue()) {
      case 1:
        addMessageComponent(requestedStrEntityTypes);
        break;
      default:
        addMessageComponent(requestedIntEntityTypes);
    }
  }

  public AKConnect(MessageProto proto) {
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
   * Get the name of the agent making this request.
   *
   * @return The agent name.
   */
  public String getAgentName() {
    return agentName.getValue();
  }

  /**
   * Get the requested entity types.
   *
   * @return The requested entity types.
   */
  public List<Integer> getRequestedEntityTypes() {
    return requestedIntEntityTypes.getValues();
  }
}