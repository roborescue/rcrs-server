package rescuecore2.standard.messages;

import java.io.InputStream;
import java.io.IOException;

import org.json.JSONObject;

import rescuecore2.messages.AbstractCommand;
import rescuecore2.messages.components.EntityIDComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent Clear command.
 */
public class AKClear extends AbstractCommand {

	private EntityIDComponent target;

	/**
	 * An AKClear message that populates its data from a stream.
	 *
	 * @param in The InputStream to read.
	 * @throws IOException If there is a problem reading the stream.
	 */
	public AKClear(InputStream in) throws IOException {
		this();
		read(in);
	}

	/**
	 * Construct an AKClear command.
	 *
	 * @param agent  The ID of the agent issuing the command.
	 * @param time   The time the command was issued.
	 * @param target The id of the entity to clear.
	 */
	public AKClear(EntityID agent, int time, EntityID target) {
		this();
		setAgentID(agent);
		setTime(time);
		this.target.setValue(target);
	}

	private AKClear() {
		super(StandardMessageURN.AK_CLEAR);
		target = new EntityIDComponent(StandardMessageComponentURN.Target);
		addMessageComponent(target);
	}

	public AKClear(MessageProto proto) {
		this();
		fromMessageProto(proto);
	}

	/**
	 * Get the desired target.
	 *
	 * @return The target ID.
	 */
	public EntityID getTarget() {
		return target.getValue();
	}

	@Override
	public JSONObject toJson() {
		JSONObject jsonObject = super.toJson();
		jsonObject.put("Target", this.getTarget());

		return jsonObject;
	}
}
