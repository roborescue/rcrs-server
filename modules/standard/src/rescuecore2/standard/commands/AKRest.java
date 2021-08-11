package rescuecore2.standard.commands;

import java.util.HashMap;
import java.util.Map;
import rescuecore2.commands.AbstractCommand;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent rest command.
 */
public class AKRest extends AbstractCommand {

  /**
   * An AKRest message that populates its data from a mapping object.
   *
   * @param fields
   *          The mapping content to set the command object.
   */
  public AKRest( Map<String, Object> fields ) {
    super( StandardCommandURN.AK_REST );
    this.setFields( fields );
  }


  /**
   * Construct a rest command.
   *
   * @param agentID
   *          The ID of the agent issuing the command.
   * @param time
   *          The time the command was issued.
   */
  public AKRest( EntityID agentID, int time ) {
    super( StandardCommandURN.AK_REST, agentID, time );
  }


  /**
   * An AKRest message.
   *
   */
  public AKRest() {
    super( StandardCommandURN.AK_REST );
  }


  @Override
  public void setFields( Map<String, Object> fields ) {
    this.setAgentID( new EntityID( (int) fields.get( "agent_id" ) ) );
    this.setTime( (int) fields.get( "time" ) );
  }


  @Override
  public Map<String, Object> getFields() {
    HashMap<String, Object> fields = new HashMap<String, Object>( 3 );

    fields.put( "agent_id", this.getAgentID().getValue() );
    fields.put( "time", this.getTime() );

    return fields;
  }
}