package rescuecore2.standard.messages;

import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.AbstractCommand;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.components.RawDataComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent speak (channel) command.
 */
public class AKSpeak extends AbstractCommand {

	private IntComponent channel;
	private RawDataComponent data;

	/**
	 * An AKSpeak message that populates its data from a stream.
	 *
	 * @param in The InputStream to read.
	 * @throws IOException If there is a problem reading the stream.
	 */
	public AKSpeak(InputStream in) throws IOException {
		this();
		read(in);
	}

	/**
	 * Construct a speak command.
	 *
	 * @param agent   The ID of the agent issuing the command.
	 * @param time    The time the command was issued.
	 * @param channel The ID of the channel to speak on.
	 * @param data    The content of the message.
	 */
	public AKSpeak(EntityID agent, int time, int channel, byte[] data) {
		this();
		setAgentID(agent);
		setTime(time);
		this.channel.setValue(channel);
		this.data.setData(data);
	}

	private AKSpeak() {
		super(StandardMessageURN.AK_SPEAK);
		channel = new IntComponent(StandardMessageComponentURN.Channel);
		data = new RawDataComponent(StandardMessageComponentURN.Message);
		addMessageComponent(channel);
		addMessageComponent(data);
	}

	public AKSpeak(MessageProto proto) {
		this();
		fromMessageProto(proto);
	}

	/**
	 * Get the channel that was used.
	 *
	 * @return The channel.
	 */
	public int getChannel() {
		return channel.getValue();
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
