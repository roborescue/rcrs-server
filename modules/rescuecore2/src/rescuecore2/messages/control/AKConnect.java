package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.components.IntListComponent;
import rescuecore2.messages.components.StringComponent;
import rescuecore2.messages.components.StringListComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.registry.Registry;

import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;
import java.io.IOException;

/**
 * A message for connecting an agent to the kernel.
 */
public class AKConnect extends AbstractMessage implements Control {
	private IntComponent requestID;
	private IntComponent version;
	private StringComponent agentName;
	private StringListComponent requestedEntityTypes_v1;
	private IntListComponent requestedEntityTypes;

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
	 * An AKConnect with particular version, requestID and requested entity
	 * types.
	 * 
	 * @param requestID            The request ID.
	 * @param version              The version number.
	 * @param agentName            The name of the agent.
	 * @param requestedEntityTypes The set of requested entity types.
	 */
	public AKConnect(int requestID, int version, String agentName,
			int[] requestedEntityTypes) {
		this();
		this.requestID.setValue(requestID);
		this.version.setValue(version);
		this.agentName.setValue(agentName);
		this.requestedEntityTypes.setValues(requestedEntityTypes);
		if (version == 1) {
			ArrayList<String> newdata = new ArrayList<>();
			for (int re : requestedEntityTypes)
				newdata.add(Registry.getCurrentRegistry().toURN_V1(re));
			this.requestedEntityTypes_v1.setValues(newdata);
		}
	}

	private AKConnect() {
		super(ControlMessageURN.AK_CONNECT);
		requestID = new IntComponent("Request ID");
		version = new IntComponent("Version");
		agentName = new StringComponent("Name");
		requestedEntityTypes_v1 = new StringListComponent(
				"Requested entity types v1");
		requestedEntityTypes = new IntListComponent(
				"Requested entity types");
		addMessageComponent(requestID);
		addMessageComponent(version);
		addMessageComponent(agentName);
		if (version.getValue() == 1)
			addMessageComponent(requestedEntityTypes_v1);
		else
			addMessageComponent(requestedEntityTypes);
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
		if (version.getValue() == 1) {
			List<Integer> list = new ArrayList<>();
			for (String str : requestedEntityTypes_v1.getValues()) {
				list.add(Registry.getCurrentRegistry().toURN_V2(str));
			}
			return list;
		}
		return requestedEntityTypes.getValues();
	}
}
