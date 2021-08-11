package rescuecore2.standard.commands;

import java.util.HashMap;
import java.util.Map;
import rescuecore2.commands.AbstractCommand;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent Unload command.
 */
public class AKUnload extends AbstractCommand {

  /**
   * An AKUnload message that populates its data from a mapping object.
   *
   * @param fields
   *          The mapping content to set the command object.
   */
  public AKUnload( Map<String, Object> fields ) {
    super( StandardCommandURN.AK_UNLOAD );
    this.setFields( fields );
  }


  /**
   * Construct an AKUnload command.
   *
   * @param agentID
   *          The ID of the agent issuing the command.
   * @param time
   *          The time the command was issued.
   */
  public AKUnload( EntityID agentID, int time ) {
    super( StandardCommandURN.AK_UNLOAD, agentID, time );
  }


  /**
   * An AKUnload message.
   *
   */
  public AKUnload() {
    super( StandardCommandURN.AK_UNLOAD );
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