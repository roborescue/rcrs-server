package rescuecore2.standard.messages;

import java.io.InputStream;
import java.io.IOException;

import org.json.JSONObject;

import rescuecore2.messages.AbstractCommand;
import rescuecore2.messages.components.EntityIDComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent Extinguish command.
 */
public class AKExtinguish extends AbstractCommand {

	private EntityIDComponent target;
	private IntComponent water;

	/**
	 * An AKExtinguish message that populates its data from a stream.
	 * 
	 * @param in The InputStream to read.
	 * @throws IOException If there is a problem reading the stream.
	 */
	public AKExtinguish(InputStream in) throws IOException {
		this();
		read(in);
	}

	/**
	 * Construct an AKExtinguish command.
	 * 
	 * @param agent  The ID of the agent issuing the command.
	 * @param time   The time the command was issued.
	 * @param target The id of the entity to extinguish.
	 * @param water  The amount of water to use.
	 */
	public AKExtinguish(EntityID agent, int time, EntityID target, int water) {
		this();
		setAgentID(agent);
		setTime(time);
		this.target.setValue(target);
		this.water.setValue(water);
	}

	private AKExtinguish() {
		super(StandardMessageURN.AK_EXTINGUISH);
		target = new EntityIDComponent(StandardMessageComponentURN.Target);
		water = new IntComponent(StandardMessageComponentURN.Water);
		addMessageComponent(target);
		addMessageComponent(water);
	}

	public AKExtinguish(MessageProto proto) {
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

	/**
	 * Get the amount of water.
	 * 
	 * @return The amount of water to use.
	 */
	public int getWater() {
		return water.getValue();
	}

	@Override
	public JSONObject toJson() {
		JSONObject jsonObject = super.toJson();
		jsonObject.put("Target", this.getTarget());

		return jsonObject;
	}
}
