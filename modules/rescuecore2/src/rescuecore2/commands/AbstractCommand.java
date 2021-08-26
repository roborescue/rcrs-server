package rescuecore2.commands;

import java.util.Map;

import rescuecore2.worldmodel.EntityID;

/**
 * An abstract base class for Command objects.
 */
public abstract class AbstractCommand implements Command {

  private String urn;
  private EntityID agentID;
  private int time;

  /**
   * Construct a new abstract command.
   *
   * @param urn The urn of the command.
   */
  protected AbstractCommand(String urn) {
    this.urn = urn;
  }

  /**
   * Construct a new abstract command.
   *
   * @param urn The urn of the command.
   */
  protected AbstractCommand(Enum<?> urn) {
    this(urn.toString());
  }

  /**
   * Construct a new abstract command.
   *
   * @param urn     The urn of the command.
   * @param agentID The ID of the agent issuing the command.
   * @param time    The time this command was issued.
   */
  protected AbstractCommand(String urn, EntityID agentID, int time) {
    this.urn = urn;
    init(agentID, time);
  }

  /**
   * Construct a new abstract command.
   *
   * @param urn     The urn of the command.
   * @param agentID The ID of the agent issuing the command.
   * @param time    The time this command was issued.
   */
  protected AbstractCommand(Enum<?> urn, EntityID agentID, int time) {
    this(urn.toString());
    init(agentID, time);
  }

  @Override
  public String getURN() {
    return this.urn.toString();
  }

  @Override
  public EntityID getAgentID() {
    return agentID;
  }

  @Override
  public int getTime() {
    return time;
  }

  /**
   * Set the ID of the agent issuing the command.
   *
   * @param agentID The new agent ID.
   */
  protected void setAgentID(EntityID agentID) {
    this.agentID = agentID;
  }

  /**
   * Set the time of the command.
   *
   * @param time The new time.
   */
  protected void setTime(int time) {
    this.time = time;
  }

  private void init(EntityID agentID, int time) {
    this.agentID = agentID;
    this.time = time;
  }

  @Override
  public abstract void setFields(Map<String, Object> fields);

  @Override
  public abstract Map<String, Object> getFields();
}