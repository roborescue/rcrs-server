package rescuecore2.standard.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import rescuecore2.commands.AbstractCommand;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent move command.
 */
public class AKMove extends AbstractCommand {

  private List<EntityID> path;
  private int x;
  private int y;

  /**
   * An AKMove message that populates its data from a mapping object.
   *
   * @param fields The mapping content to set the command object.
   */
  public AKMove(Map<String, Object> fields) {
    super(StandardCommandURN.AK_MOVE);
    this.setFields(fields);
  }

  /**
   * Construct a move command.
   *
   * @param agentID The ID of the agent issuing the command.
   * @param time    The time the command was issued.
   * @param path    The path to move.
   */
  public AKMove(EntityID agentID, int time, List<EntityID> path) {
    super(StandardCommandURN.AK_MOVE, agentID, time);
    this.path = path;
    this.x = -1;
    this.y = -1;
  }

  /**
   * Construct a move command.
   *
   * @param agentID      The ID of the agent issuing the command.
   * @param time         The time the command was issued.
   * @param path         The path to move.
   * @param destinationX The X coordinate of the desired destination.
   * @param destinationY The Y coordinate of the desired destination.
   */
  public AKMove(EntityID agentID, int time, List<EntityID> path, int destinationX, int destinationY) {
    super(StandardCommandURN.AK_MOVE, agentID, time);

    this.path = path;
    this.x = destinationX;
    this.y = destinationY;
  }

  /**
   * An AKMove message.
   *
   */
  public AKMove() {
    super(StandardCommandURN.AK_MOVE);
  }

  /**
   * Get the desired movement path.
   *
   * @return The movement path.
   */
  public List<EntityID> getPath() {
    return this.path;
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

    this.path = new ArrayList<EntityID>();
    if (fields.get("path") instanceof int[]) {
      int[] path = (int[]) fields.get("path");
      for (int i = 0; i < path.length; i++) {
        this.path.add(new EntityID(path[i]));
      }
    }

    this.x = (int) fields.get("x");
    this.y = (int) fields.get("y");
  }

  @Override
  public Map<String, Object> getFields() {
    HashMap<String, Object> fields = new HashMap<String, Object>(3);

    fields.put("agent_id", this.getAgentID().getValue());
    fields.put("time", this.getTime());

    int[] path = new int[this.path.size()];
    for (int i = 0; i < this.path.size(); i++) {
      path[i] = this.path.get(i).getValue();
    }
    fields.put("path", path);

    fields.put("x", this.x);
    fields.put("y", this.y);

    return fields;
  }

  @Override
  public JSONObject toJson() {
    JSONObject jsonObject = super.toJson();
    jsonObject.put("Path", this.getPath());
    jsonObject.put("X", this.getDestinationX());
    jsonObject.put("Y", this.getDestinationX());

    return jsonObject;
  }
}