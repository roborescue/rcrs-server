package rescuecore2.standard.messages;

import java.io.InputStream;
import java.io.IOException;

import org.json.JSONObject;

import rescuecore2.messages.AbstractCommand;
import rescuecore2.messages.components.EntityIDComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent Rescue command.
 */
public class AKRescue extends AbstractCommand {

	private EntityIDComponent target;

	/**
	 * An AKRescue message that populates its data from a stream.
	 *
	 * @param in The InputStream to read.
	 * @throws IOException If there is a problem reading the stream.
	 */
	public AKRescue(InputStream in) throws IOException {
		this();
		read(in);
	}

	/**
	 * Construct an AKRescue command.
	 *
	 * @param agent  The ID of the agent issuing the command.
	 * @param time   The time the command was issued.
	 * @param target The id of the entity to rescue.
	 */
	public AKRescue(EntityID agent, int time, EntityID target) {
		this();
		setAgentID(agent);
		setTime(time);
		this.target.setValue(target);
	}

	private AKRescue() {
		super(StandardMessageURN.AK_RESCUE);
		target = new EntityIDComponent(StandardMessageComponentURN.Target);
		addMessageComponent(target);
	}

	public AKRescue(MessageProto proto) {
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
