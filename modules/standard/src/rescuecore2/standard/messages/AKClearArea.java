package rescuecore2.standard.messages;

import java.io.InputStream;
import java.io.IOException;

import org.json.JSONObject;

import rescuecore2.messages.AbstractCommand;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent Clear command.
 */
public class AKClearArea extends AbstractCommand {

	private IntComponent x;
	private IntComponent y;

	/**
	 * An AKClearArea message that populates its data from a stream.
	 *
	 * @param in The InputStream to read.
	 * @throws IOException If there is a problem reading the stream.
	 */
	public AKClearArea(InputStream in) throws IOException {
		this();
		read(in);
	}

	/**
	 * Construct an AKClearArea command.
	 *
	 * @param agent        The ID of the agent issuing the command.
	 * @param time         The time the command was issued.
	 * @param destinationX The X coordinate of the desired destination to clear.
	 * @param destinationY The Y coordinate of the desired destination to clear.
	 */
	public AKClearArea(EntityID agent, int time, int destinationX,
			int destinationY) {
		this();
		setAgentID(agent);
		setTime(time);
		this.x.setValue(destinationX);
		this.y.setValue(destinationY);
	}

	private AKClearArea() {
		super(StandardMessageURN.AK_CLEAR_AREA);
		x = new IntComponent(StandardMessageComponentURN.DestinationX);
		y = new IntComponent(StandardMessageComponentURN.DestinationY);
		addMessageComponent(x);
		addMessageComponent(y);
	}

	public AKClearArea(MessageProto proto) {
		this();
		fromMessageProto(proto);
	}

	/**
	 * Get the destination X coordinate.
	 *
	 * @return The destination X coordination.
	 */
	public int getDestinationX() {
		return x.getValue();
	}

	/**
	 * Get the destination Y coordinate.
	 *
	 * @return The destination Y coordination.
	 */
	public int getDestinationY() {
		return y.getValue();
	}

	@Override
	public JSONObject toJson() {
		JSONObject jsonObject = super.toJson();
		jsonObject.put("X", this.getDestinationX());
		jsonObject.put("Y", this.getDestinationY());

		return jsonObject;
	}
}