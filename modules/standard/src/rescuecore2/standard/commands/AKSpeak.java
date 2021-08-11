package rescuecore2.standard.commands;

import java.util.HashMap;
import java.util.Map;
import rescuecore2.commands.AbstractCommand;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent speak (channel) command.
 */
public class AKSpeak extends AbstractCommand {

  private int    channel;
  private byte[] data;


  /**
   * An AKSpeak message that populates its data from a mapping object.
   *
   * @param fields
   *          The mapping content to set the command object.
   */
  public AKSpeak( Map<String, Object> fields ) {
    super( StandardCommandURN.AK_SPEAK );
    this.setFields( fields );
  }


  /**
   * Construct a speak command.
   *
   * @param agentID
   *          The ID of the agent issuing the command.
   * @param time
   *          The time the command was issued.
   * @param channel
   *          The ID of the channel to speak on.
   * @param data
   *          The content of the message.
   */
  public AKSpeak( EntityID agentID, int time, int channel, byte[] data ) {
    super( StandardCommandURN.AK_SPEAK, agentID, time );

    this.channel = channel;
    this.data = data;
  }


  /**
   * An AKSpeak message.
   *
   */
  public AKSpeak() {
    super( StandardCommandURN.AK_SPEAK );
  }


  /**
   * Get the channel that was used.
   *
   * @return The channel.
   */
  public int getChannel() {
    return this.channel;
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
    this.channel = (int) fields.get( "channel" );
    this.data = (byte[]) fields.get( "data" );
  }


  @Override
  public Map<String, Object> getFields() {
    HashMap<String, Object> fields = new HashMap<String, Object>( 3 );

    fields.put( "agent_id", this.getAgentID().getValue() );
    fields.put( "time", this.getTime() );
    fields.put( "channel", this.channel );
    fields.put( "data", this.data );

    return fields;
  }
}