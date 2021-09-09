package rescuecore2.standard.commands;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import rescuecore2.commands.AbstractCommand;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent Load command.
 */
public class AKLoad extends AbstractCommand {

  private EntityID target;

  /**
   * An AKLoad message that populates its data from a mapping object.
   *
   * @param fields The mapping content to set the command object.
   */
  public AKLoad(Map<String, Object> fields) {
    super(StandardCommandURN.AK_LOAD);
    this.setFields(fields);
  }

  /**
   * Construct an AKLoad command.
   *
   * @param agentID The ID of the agent issuing the command.
   * @param time    The time the command was issued.
   * @param target  The id of the entity to load.
   */
  public AKLoad(EntityID agentID, int time, EntityID target) {
    super(StandardCommandURN.AK_LOAD, agentID, time);
    this.target = target;
  }

  /**
   * An AKLoad message.
   *
   */
  public AKLoad() {
    super(StandardCommandURN.AK_LOAD);
  }

  /**
   * Get the desired target.
   *
   * @return The target ID.
   */
  public EntityID getTarget() {
    return this.target;
  }

  @Override
  public void setFields(Map<String, Object> fields) {
    this.setAgentID(new EntityID((int) fields.get("agent_id")));
    this.setTime((int) fields.get("time"));
    this.target = new EntityID((int) fields.get("target_id"));
  }

  @Override
  public Map<String, Object> getFields() {
    HashMap<String, Object> fields = new HashMap<String, Object>(3);

    fields.put("agent_id", this.getAgentID().getValue());
    fields.put("time", this.getTime());
    fields.put("target_id", this.target.getValue());

    return fields;
  }

  @Override
  public JSONObject toJson() {
    JSONObject jsonObject = super.toJson();
    jsonObject.put("Target", this.getTarget());

    return jsonObject;
  }
}