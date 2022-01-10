package rescuecore2.standard.messages;

import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.AbstractCommand;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.components.RawDataComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent tell command.
 */
public class AKTell extends AbstractCommand {

	private IntComponent channel;
	private RawDataComponent data;

	/**
	 * An AKTell message that populates its data from a stream.
	 *
	 * @param in The InputStream to read.
	 * @throws IOException If there is a problem reading the stream.
	 */
	public AKTell(InputStream in) throws IOException {
		this();
		read(in);
	}

	/**
	 * Construct an AKTell command.
	 *
	 * @param agent The ID of the agent issuing the command.
	 * @param time  The time the command was issued.
	 * @param data  The content of the command.
	 */
	public AKTell(EntityID agent, int time, byte[] data) {
		this();
		setAgentID(agent);
		setTime(time);
		this.data.setData(data);
	}

	private AKTell() {
		super(StandardMessageURN.AK_TELL);
		channel = new IntComponent(StandardMessageComponentURN.Channel);
		data = new RawDataComponent(StandardMessageComponentURN.Message);
		addMessageComponent(channel);
		addMessageComponent(data);
	}

	public AKTell(MessageProto proto) {
		this();
		fromMessageProto(proto);
	}

	/**
	 * Get the content of the message.
	 *
	 * @return The message content.
	 */
	public byte[] getContent() {
		return data.getData();
	}
}
