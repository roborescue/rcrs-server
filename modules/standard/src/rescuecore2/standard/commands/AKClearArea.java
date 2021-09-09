package rescuecore2.standard.commands;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import rescuecore2.commands.AbstractCommand;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent Clear command.
 */
public class AKClearArea extends AbstractCommand {

  private int x;
  private int y;

  /**
   * An AKClearArea message that populates its data from a mapping object.
   *
   * @param fields The mapping content to set the command object.
   */
  public AKClearArea(Map<String, Object> fields) {
    super(StandardCommandURN.AK_CLEAR_AREA);
    this.setFields(fields);
  }

  /**
   * Construct an AKClearArea command.
   *
   * @param agentID      The ID of the agent issuing the command.
   * @param time         The time the command was issued.
   * @param destinationX The X coordinate of the desired destination to clear.
   * @param destinationY The Y coordinate of the desired destination to clear.
   */
  public AKClearArea(EntityID agentID, int time, int destinationX, int destinationY) {
    super(StandardCommandURN.AK_CLEAR_AREA, agentID, time);

    this.x = destinationX;
    this.y = destinationY;
  }

  /**
   * An AKClearArea message.
   *
   */
  public AKClearArea() {
    super(StandardCommandURN.AK_CLEAR_AREA);
  }

  /**
   * Get the destination X coordinate.
   *
   * @return The destination X coordination.
   */
  public int getDestinationX() {
    return this.x;
  }

  /**
   * Get the destination Y coordinate.
   *
   * @return The destination Y coordination.
   */
  public int getDestinationY() {
    return this.y;
  }

  @Override
  public void setFields(Map<String, Object> fields) {
    this.setAgentID(new EntityID((int) fields.get("agent_id")));
    this.setTime((int) fields.get("time"));
    this.x = (int) fields.get("x");
    this.y = (int) fields.get("y");
  }

  @Override
  public Map<String, Object> getFields() {
    HashMap<String, Object> fields = new HashMap<String, Object>(3);

    fields.put("agent_id", this.getAgentID().getValue());
    fields.put("time", this.getTime());
    fields.put("x", this.x);
    fields.put("y", this.y);

    return fields;
  }

  @Override
  public JSONObject toJson() {
    JSONObject jsonObject = super.toJson();
    jsonObject.put("X", this.getDestinationX());
    jsonObject.put("Y", this.getDestinationY());

    return jsonObject;
  }
}