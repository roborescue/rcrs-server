package rescuecore2.standard.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rescuecore2.commands.AbstractCommand;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent channel subscription command.
 */
public class AKSubscribe extends AbstractCommand {

  private List<Integer> channels;


  /**
   * An AKSubscribe message that populates its data from a mapping object.
   *
   * @param fields
   *          The mapping content to set the command object.
   */
  public AKSubscribe( Map<String, Object> fields ) {
    super( StandardCommandURN.AK_SUBSCRIBE );
    this.setFields( fields );
  }


  /**
   * Construct a subscribe command.
   *
   * @param agentID
   *          The ID of the agent issuing the command.
   * @param time
   *          The time the command was issued.
   * @param channels
   *          The IDs of the channels to speak on.
   */
  public AKSubscribe( EntityID agentID, int time, int... channels ) {
    super( StandardCommandURN.AK_SUBSCRIBE, agentID, time );

    this.channels = new ArrayList<Integer>();
    for ( int i = 0; i < channels.length; i++ ) {
      this.channels.add( channels[i] );
    }
  }


  /**
   * An AKSubscribe message.
   *
   */
  public AKSubscribe() {
    super( StandardCommandURN.AK_SUBSCRIBE );
  }


  /**
   * Get the channels that have been requested.
   *
   * @return The requested channels.
   */
  public List<Integer> getChannels() {
    return this.channels;
  }


  @Override
  public void setFields( Map<String, Object> fields ) {
    this.setAgentID( new EntityID( (int) fields.get( "agent_id" ) ) );
    this.setTime( (int) fields.get( "time" ) );

    this.channels = new ArrayList<Integer>();
    if ( fields.get( "channels" ) instanceof int[] ) {
      int[] channels = (int[]) fields.get( "channels" );
      for ( int i = 0; i < channels.length; i++ ) {
        this.channels.add( channels[i] );
      }
    }
  }


  @Override
  public Map<String, Object> getFields() {
    HashMap<String, Object> fields = new HashMap<String, Object>( 3 );

    fields.put( "agent_id", this.getAgentID().getValue() );
    fields.put( "time", this.getTime() );

    int[] channels = new int[this.channels.size()];
    for ( int i = 0; i < this.channels.size(); i++ ) {
      channels[i] = this.channels.get( i );
    }
    fields.put( "channels", channels );

    return fields;
  }
}