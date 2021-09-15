package rescuecore2.standard.messages;

import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.AbstractCommand;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent rest command.
 */
public class AKRest extends AbstractCommand {

	/**
	 * An AKRest message that populates its data from a stream.
	 *
	 * @param in The InputStream to read.
	 * @throws IOException If there is a problem reading the stream.
	 */
	public AKRest(InputStream in) throws IOException {
		this();
		read(in);
	}

	/**
	 * Construct a rest command.
	 *
	 * @param agent The ID of the agent issuing the command.
	 * @param time  The time the command was issued.
	 */
	public AKRest(EntityID agent, int time) {
		this();
		setAgentID(agent);
		setTime(time);
	}

	private AKRest() {
		super(StandardMessageURN.AK_REST);
	}

	public AKRest(MessageProto proto) {
		this();
		fromMessageProto(proto);
	}

}
