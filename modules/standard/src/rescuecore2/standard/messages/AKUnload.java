package rescuecore2.standard.messages;

import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.AbstractCommand;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent Unload command.
 */
public class AKUnload extends AbstractCommand {

	/**
	 * An AKUnload message that populates its data from a stream.
	 *
	 * @param in The InputStream to read.
	 * @throws IOException If there is a problem reading the stream.
	 */
	public AKUnload(InputStream in) throws IOException {
		this();
		read(in);
	}

	/**
	 * Construct an unload command.
	 *
	 * @param agentID The ID of the agent issuing the command.
	 * @param time    The time the command was issued.
	 */
	public AKUnload(EntityID agentID, int time) {
		this();
		setAgentID(agentID);
		setTime(time);
	}

	private AKUnload() {
		super(StandardMessageURN.AK_UNLOAD);
	}

	public AKUnload(MessageProto proto) {
		this();
		fromMessageProto(proto);
	}

}
