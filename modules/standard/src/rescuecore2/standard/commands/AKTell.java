package rescuecore2.standard.commands;

import java.util.HashMap;
import java.util.Map;
import rescuecore2.commands.AbstractCommand;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent tell command.
 */
public class AKTell extends AbstractCommand {

  private byte[] data;


  /**
   * An AKTell message that populates its data from a mapping object.
   *
   * @param fields
   *          The mapping content to set the command object.
   */
  public AKTell( Map<String, Object> fields ) {
    super( StandardCommandURN.AK_TELL );
    this.setFields( fields );
  }


  /**
   * Construct an AKTell command.
   *
   * @param agentID
   *          The ID of the agent issuing the command.
   * @param time
   *          The time the command was issued.
   * @param data
   *          The content of the command.
   */
  public AKTell( EntityID agentID, int time, byte[] data ) {
    super( StandardCommandURN.AK_TELL, agentID, time );

    this.data = data;
  }


  /**
   * An AKTell message.
   *
   */
  public AKTell() {
    super( StandardCommandURN.AK_TELL );
  }


  /**
   * Get the content of the message.
   *
   * @return The message content.
   */
  public byte[] getContent() {
    return this.data;
  }


  @Override
  public void setFields( Map<String, Object> fields ) {
    this.setAgentID( new EntityID( (int) fields.get( "agent_id" ) ) );
    this.setTime( (int) fields.get( "time" ) );
    this.data = (byte[]) fields.get( "data" );
  }


  @Override
  public Map<String, Object> getFields() {
    HashMap<String, Object> fields = new HashMap<String, Object>( 3 );

    fields.put( "agent_id", this.getAgentID().getValue() );
    fields.put( "time", this.getTime() );
    fields.put( "data", this.data );

    return fields;
  }
}