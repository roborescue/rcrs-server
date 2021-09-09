package rescuecore2.commands;

import java.util.Map;

import org.json.JSONObject;

import rescuecore2.worldmodel.EntityID;

/**
 * An interface of Command of agents.
 */
public interface Command {

  /**
   * Get the urn of this command type.
   *
   * @return The command urn.
   */
  String getURN();

  /**
   * Get the id of the agent-controlled entity that has issued this command.
   *
   * @return The id of the agent.
   */
  EntityID getAgentID();

  /**
   * Get the timestep this command is intended for.
   *
   * @return The timestep.
   */
  int getTime();

  /**
   * Set this command with the mapping content.
   *
   * @param fields The mapping content to set the command object.
   */
  void setFields(Map<String, Object> fields);

  /**
   * Get the command in the mapping content format
   *
   * @return The command object in the mapping content format.
   *
   */
  Map<String, Object> getFields();

  /**
   * Get the command in JSON format
   *
   * @return The command in JSON format
   */
  JSONObject toJson();
}