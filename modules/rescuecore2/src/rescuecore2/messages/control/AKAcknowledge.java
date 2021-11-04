package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.EntityIDComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.EntityID;

/**
 * A message for acknowleding a connection to the kernel.
 */
public class AKAcknowledge extends AbstractMessage {
	private IntComponent requestID;
	private EntityIDComponent agentID;

	/**
	 * An AKAcknowledge message that populates its data from a stream.
	 *
	 * @param in The InputStream to read.
	 * @throws IOException If there is a problem reading the stream.
	 */
	public AKAcknowledge(InputStream in) throws IOException {
		this();
		read(in);
	}

	/**
	 * AKAcknowledge message with specific request ID and agent ID components.
	 *
	 * @param requestID The request ID.
	 * @param agentID   The agent ID.
	 */
	public AKAcknowledge(int requestID, EntityID agentID) {
		this();
		this.requestID.setValue(requestID);
		this.agentID.setValue(agentID);
	}

	private AKAcknowledge() {
		super(ControlMessageURN.AK_ACKNOWLEDGE);
		requestID = new IntComponent(ControlMessageComponentURN.RequestID);
		agentID = new EntityIDComponent(ControlMessageComponentURN.AgentID);
		addMessageComponent(requestID);
		addMessageComponent(agentID);
	}

	public AKAcknowledge(MessageProto proto) {
		this();
		fromMessageProto(proto);
	}

	/**
	 * Get the request ID of this acknowledgement.
	 *
	 * @return The request ID component.
	 */
	public int getRequestID() {
		return requestID.getValue();
	}

	/**
	 * Get the agent ID of this acknowledgement.
	 *
	 * @return The agent ID component.
	 */
	public EntityID getAgentID() {
		return agentID.getValue();
	}
}
