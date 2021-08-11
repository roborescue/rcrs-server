package rescuecore2.standard.commands;

import java.util.HashMap;
import java.util.Map;
import rescuecore2.commands.AbstractCommand;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent Clear command.
 */
public class AKClear extends AbstractCommand {

  private EntityID target;


  /**
   * An AKClear message that populates its data from a mapping object.
   *
   * @param fields
   *          The mapping content to set the command object.
   */
  public AKClear( Map<String, Object> fields ) {
    super( StandardCommandURN.AK_CLEAR );
    this.setFields( fields );
  }


  /**
   * Construct an AKClear command.
   *
   * @param agentID
   *          The ID of the agent issuing the command.
   * @param time
   *          The time the command was issued.
   * @param target
   *          The id of the entity to clear.
   */
  public AKClear( EntityID agentID, int time, EntityID target ) {
    super( StandardCommandURN.AK_CLEAR, agentID, time );

    this.target = target;
  }


  /**
   * An AKClear message.
   *
   */
  public AKClear() {
    super( StandardCommandURN.AK_CLEAR );
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
  public void setFields( Map<String, Object> fields ) {
    this.setAgentID( new EntityID( (int) fields.get( "agent_id" ) ) );
    this.setTime( (int) fields.get( "time" ) );
    this.target = new EntityID( (int) fields.get( "target_id" ) );
  }


  @Override
  public Map<String, Object> getFields() {
    HashMap<String, Object> fields = new HashMap<String, Object>( 3 );

    fields.put( "agent_id", this.getAgentID().getValue() );
    fields.put( "time", this.getTime() );
    fields.put( "target_id", this.getTarget().getValue() );

    return fields;
  }
}