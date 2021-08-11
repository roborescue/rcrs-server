package rescuecore2.standard.commands;

import java.util.HashMap;
import java.util.Map;
import rescuecore2.commands.AbstractCommand;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent Extinguish command.
 */
public class AKExtinguish extends AbstractCommand {

  private EntityID target;
  private int      water;


  /**
   * An AKExtinguish message that populates its data from a mapping object.
   *
   * @param fields
   *          The mapping content to set the command object.
   */
  public AKExtinguish( Map<String, Object> fields ) {
    super( StandardCommandURN.AK_EXTINGUISH );
    this.setFields( fields );
  }


  /**
   * Construct an AKExtinguish command.
   *
   * @param agentID
   *          The ID of the agent issuing the command.
   * @param time
   *          The time the command was issued.
   * @param target
   *          The id of the entity to extinguish.
   * @param water
   *          The amount of water to use.
   */
  public AKExtinguish( EntityID agentID, int time, EntityID target, int water ) {
    super( StandardCommandURN.AK_EXTINGUISH, agentID, time );

    this.target = target;
    this.water = water;
  }


  /**
   * An AKExtinguish message.
   *
   */
  public AKExtinguish() {
    super( StandardCommandURN.AK_EXTINGUISH );
  }


  /**
   * Get the desired target.
   *
   * @return The target ID.
   */
  public EntityID getTarget() {
    return this.target;
  }


  /**
   * Get the amount of water.
   *
   * @return The amount of water to use.
   */
  public int getWater() {
    return this.water;
  }


  @Override
  public void setFields( Map<String, Object> fields ) {
    this.setAgentID( new EntityID( (int) fields.get( "agent_id" ) ) );
    this.setTime( (int) fields.get( "time" ) );
    this.target = new EntityID( (int) fields.get( "target_id" ) );
    this.water = (int) fields.get( "water" );
  }


  @Override
  public Map<String, Object> getFields() {
    HashMap<String, Object> fields = new HashMap<String, Object>( 3 );

    fields.put( "agent_id", this.getAgentID().getValue() );
    fields.put( "time", this.getTime() );
    fields.put( "target_id", this.target.getValue() );
    fields.put( "water", this.water );

    return fields;
  }
}