package rescuecore2.standard.commands;

import java.util.HashMap;
import java.util.Map;
import rescuecore2.commands.AbstractCommand;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent Rescue command.
 */
public class AKRescue extends AbstractCommand {

  private EntityID target;


  /**
   * An AKRescue message that populates its data from a mapping object.
   *
   * @param fields
   *          The mapping content to set the command object.
   */
  public AKRescue( Map<String, Object> fields ) {
    super( StandardCommandURN.AK_RESCUE );
    this.setFields( fields );
  }


  /**
   * Construct an AKRescue command.
   *
   * @param agentID
   *          The ID of the agent issuing the command.
   * @param time
   *          The time the command was issued.
   * @param target
   *          The id of the entity to rescue.
   */
  public AKRescue( EntityID agentID, int time, EntityID target ) {
    super( StandardCommandURN.AK_RESCUE, agentID, time );

    this.target = target;
  }


  /**
   * An AKRescue message.
   *
   */
  public AKRescue() {
    super( StandardCommandURN.AK_RESCUE );
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
    fields.put( "target_id", this.target.getValue() );

    return fields;
  }
}