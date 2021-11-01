package rescuecore2.standard.messages;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import rescuecore2.messages.AbstractCommand;
import rescuecore2.messages.components.EntityIDComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent Load command.
 */
public class AKLoad extends AbstractCommand {

  private EntityIDComponent target;

  /**
   * An AKLoad message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public AKLoad(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * Construct an AKLoad command.
   *
   * @param agent  The ID of the agent issuing the command.
   * @param time   The time the command was issued.
   * @param target The id of the entity to load.
   */
  public AKLoad(EntityID agent, int time, EntityID target) {
    this();
    setAgentID(agent);
    setTime(time);
    this.target.setValue(target);
  }

  private AKLoad() {
    super(StandardMessageURN.AK_LOAD);
    target = new EntityIDComponent(StandardMessageComponentURN.Target);
    addMessageComponent(target);
  }

  public AKLoad(MessageProto proto) {
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
