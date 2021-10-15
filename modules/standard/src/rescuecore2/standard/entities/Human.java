package rescuecore2.standard.entities;

import org.json.JSONObject;
import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.IntProperty;

/**
 * Abstract base class for Humans.
 */
public abstract class Human extends StandardEntity {

  private IntProperty       x;
  private IntProperty       y;
  private EntityRefProperty position;
  private IntArrayProperty  positionHistory;
  private IntProperty       travelDistance;
  private IntProperty       direction;
  private IntProperty       stamina;
  private IntProperty       hp;
  private IntProperty       damage;
  private IntProperty       buriedness;


  /**
   * Construct a Human object with entirely undefined property values.
   *
   * @param id
   *          The ID of this entity.
   */
  protected Human( EntityID id ) {
    super( id );
    x = new IntProperty( StandardPropertyURN.X );
    y = new IntProperty( StandardPropertyURN.Y );
    travelDistance = new IntProperty( StandardPropertyURN.TRAVEL_DISTANCE );
    position = new EntityRefProperty( StandardPropertyURN.POSITION );
    positionHistory = new IntArrayProperty(
        StandardPropertyURN.POSITION_HISTORY );
    direction = new IntProperty( StandardPropertyURN.DIRECTION );
    stamina = new IntProperty( StandardPropertyURN.STAMINA );
    hp = new IntProperty( StandardPropertyURN.HP );
    damage = new IntProperty( StandardPropertyURN.DAMAGE );
    buriedness = new IntProperty( StandardPropertyURN.BURIEDNESS );
    registerProperties( x, y, position, positionHistory, travelDistance,
        direction, stamina, hp, damage, buriedness );
  }


  /**
   * Human copy constructor.
   *
   * @param other
   *          The Human to copy.
   */
  public Human( Human other ) {
    super( other );
    x = new IntProperty( other.x );
    y = new IntProperty( other.y );
    travelDistance = new IntProperty( other.travelDistance );
    position = new EntityRefProperty( other.position );
    positionHistory = new IntArrayProperty( other.positionHistory );
    direction = new IntProperty( other.direction );
    stamina = new IntProperty( other.stamina );
    hp = new IntProperty( other.hp );
    damage = new IntProperty( other.damage );
    buriedness = new IntProperty( other.buriedness );
    registerProperties( x, y, position, positionHistory, travelDistance,
        direction, stamina, hp, damage, buriedness );
  }


  @Override
  public Property getProperty( int urn ) {
    StandardPropertyURN type;
    try {
      type = StandardPropertyURN.fromInt( urn );
    } catch ( IllegalArgumentException e ) {
      return super.getProperty( urn );
    }
    switch ( type ) {
      case POSITION:
        return position;
      case POSITION_HISTORY:
        return positionHistory;
      case DIRECTION:
        return direction;
      case STAMINA:
        return stamina;
      case HP:
        return hp;
      case X:
        return x;
      case Y:
        return y;
      case DAMAGE:
        return damage;
      case BURIEDNESS:
        return buriedness;
      case TRAVEL_DISTANCE:
        return travelDistance;
      default:
        return super.getProperty( urn );
    }
  }


  @Override
  public Pair<Integer, Integer>
      getLocation( WorldModel<? extends StandardEntity> world ) {
    if ( x.isDefined() && y.isDefined() ) {
      return new Pair<Integer, Integer>( x.getValue(), y.getValue() );
    }
    if ( position.isDefined() ) {
      EntityID pos = getPosition();
      StandardEntity e = world.getEntity( pos );
      return e.getLocation( world );
    }
    return null;
  }


  /**
   * Get the position property.
   *
   * @return The position property.
   */
  public EntityRefProperty getPositionProperty() {
    return position;
  }


  /**
   * Get the position of this human.
   *
   * @return The position.
   */
  public EntityID getPosition() {
    return position.getValue();
  }


  /**
   * Set the position of this human.
   *
   * @param position
   *          The new position.
   */
  public void setPosition( EntityID position ) {
    this.position.setValue( position );
  }


  /**
   * Find out if the position property has been defined.
   *
   * @return True if the position property has been defined, false otherwise.
   */
  public boolean isPositionDefined() {
    return position.isDefined();
  }


  /**
   * Undefine the position property.
   */
  public void undefinePosition() {
    position.undefine();
  }


  /**
   * Get the position history property.
   *
   * @return The position history property.
   */
  public IntArrayProperty getPositionHistoryProperty() {
    return positionHistory;
  }


  /**
   * Get the position history.
   *
   * @return The position history.
   */
  public int[] getPositionHistory() {
    return positionHistory.getValue();
  }


  /**
   * Set the position history.
   *
   * @param history
   *          The new position history.
   */
  public void setPositionHistory( int[] history ) {
    this.positionHistory.setValue( history );
  }


  /**
   * Find out if the position history property has been defined.
   *
   * @return True if the position history property has been defined, false
   *         otherwise.
   */
  public boolean isPositionHistoryDefined() {
    return positionHistory.isDefined();
  }


  /**
   * Undefine the position history property.
   */
  public void undefinePositionHistory() {
    positionHistory.undefine();
  }


  /**
   * Get the direction property.
   *
   * @return The direction property.
   */
  public IntProperty getDirectionProperty() {
    return direction;
  }


  /**
   * Get the direction.
   *
   * @return The direction.
   */
  public int getDirection() {
    return direction.getValue();
  }


  /**
   * Set the direction.
   *
   * @param direction
   *          The new direction.
   */
  public void setDirection( int direction ) {
    this.direction.setValue( direction );
  }


  /**
   * Find out if the direction property has been defined.
   *
   * @return True if the direction property has been defined, false otherwise.
   */
  public boolean isDirectionDefined() {
    return direction.isDefined();
  }


  /**
   * Undefine the direction property.
   */
  public void undefineDirection() {
    direction.undefine();
  }


  /**
   * Get the stamina property.
   *
   * @return The stamina property.
   */
  public IntProperty getStaminaProperty() {
    return stamina;
  }


  /**
   * Get the stamina of this human.
   *
   * @return The stamina.
   */
  public int getStamina() {
    return stamina.getValue();
  }


  /**
   * Set the stamina of this human.
   *
   * @param stamina
   *          The new stamina.
   */
  public void setStamina( int stamina ) {
    this.stamina.setValue( stamina );
  }


  /**
   * Find out if the stamina property has been defined.
   *
   * @return True if the stamina property has been defined, false otherwise.
   */
  public boolean isStaminaDefined() {
    return stamina.isDefined();
  }


  /**
   * Undefine the stamina property.
   */
  public void undefineStamina() {
    stamina.undefine();
  }


  /**
   * Get the hp property.
   *
   * @return The hp property.
   */
  public IntProperty getHPProperty() {
    return hp;
  }


  /**
   * Get the hp of this human.
   *
   * @return The hp of this human.
   */
  public int getHP() {
    return hp.getValue();
  }


  /**
   * Set the hp of this human.
   *
   * @param newHP
   *          The new hp.
   */
  public void setHP( int newHP ) {
    this.hp.setValue( newHP );
  }


  /**
   * Find out if the hp property has been defined.
   *
   * @return True if the hp property has been defined, false otherwise.
   */
  public boolean isHPDefined() {
    return hp.isDefined();
  }


  /**
   * Undefine the hp property.
   */
  public void undefineHP() {
    hp.undefine();
  }


  /**
   * Get the damage property.
   *
   * @return The damage property.
   */
  public IntProperty getDamageProperty() {
    return damage;
  }


  /**
   * Get the damage of this human.
   *
   * @return The damage of this human.
   */
  public int getDamage() {
    return damage.getValue();
  }


  /**
   * Set the damage of this human.
   *
   * @param damage
   *          The new damage.
   */
  public void setDamage( int damage ) {
    this.damage.setValue( damage );
  }


  /**
   * Find out if the damage property has been defined.
   *
   * @return True if the damage property has been defined, false otherwise.
   */
  public boolean isDamageDefined() {
    return damage.isDefined();
  }


  /**
   * Undefine the damage property.
   */
  public void undefineDamage() {
    damage.undefine();
  }


  /**
   * Get the buriedness property.
   *
   * @return The buriedness property.
   */
  public IntProperty getBuriednessProperty() {
    return buriedness;
  }


  /**
   * Get the buriedness of this human.
   *
   * @return The buriedness of this human.
   */
  public int getBuriedness() {
    return buriedness.getValue();
  }


  /**
   * Set the buriedness of this human.
   *
   * @param buriedness
   *          The new buriedness.
   */
  public void setBuriedness( int buriedness ) {
    this.buriedness.setValue( buriedness );
  }


  /**
   * Find out if the buriedness property has been defined.
   *
   * @return True if the buriedness property has been defined, false otherwise.
   */
  public boolean isBuriednessDefined() {
    return buriedness.isDefined();
  }


  /**
   * Undefine the buriedness property.
   */
  public void undefineBuriedness() {
    buriedness.undefine();
  }


  /**
   * Get the X property.
   *
   * @return The X property.
   */
  public IntProperty getXProperty() {
    return x;
  }


  /**
   * Get the X coordinate of this human.
   *
   * @return The x coordinate of this human.
   */
  public int getX() {
    return x.getValue();
  }


  /**
   * Set the X coordinate of this human.
   *
   * @param x
   *          The new x coordinate.
   */
  public void setX( int x ) {
    this.x.setValue( x );
  }


  /**
   * Find out if the x property has been defined.
   *
   * @return True if the x property has been defined, false otherwise.
   */
  public boolean isXDefined() {
    return x.isDefined();
  }


  /**
   * Undefine the X property.
   */
  public void undefineX() {
    x.undefine();
  }


  /**
   * Get the y property.
   *
   * @return The y property.
   */
  public IntProperty getYProperty() {
    return y;
  }


  /**
   * Get the y coordinate of this human.
   *
   * @return The y coordinate of this human.
   */
  public int getY() {
    return y.getValue();
  }


  /**
   * Set the y coordinate of this human.
   *
   * @param y
   *          The new y coordinate.
   */
  public void setY( int y ) {
    this.y.setValue( y );
  }


  /**
   * Find out if the y property has been defined.
   *
   * @return True if the y property has been defined, false otherwise.
   */
  public boolean isYDefined() {
    return y.isDefined();
  }


  /**
   * Undefine the y property.
   */
  public void undefineY() {
    y.undefine();
  }


  /**
   * Get the travel distance property.
   *
   * @return The travel distance property.
   */
  public IntProperty getTravelDistanceProperty() {
    return travelDistance;
  }


  /**
   * Get the travel distance.
   *
   * @return The travel distance.
   */
  public int getTravelDistance() {
    return travelDistance.getValue();
  }


  /**
   * Set the travel distance.
   *
   * @param d
   *          The new travel distance.
   */
  public void setTravelDistance( int d ) {
    this.travelDistance.setValue( d );
  }


  /**
   * Find out if the travel distance property has been defined.
   *
   * @return True if the travel distance property has been defined, false
   *         otherwise.
   */
  public boolean isTravelDistanceDefined() {
    return travelDistance.isDefined();
  }


  /**
   * Undefine the travel distance property.
   */
  public void undefineTravelDistance() {
    travelDistance.undefine();
  }


  /**
   * Get the entity represented by the position property. The result will be
   * null if the position property has not been set or if the entity reference
   * is invalid.
   *
   * @param model
   *          The WorldModel to look up entity references.
   * @return The entity represented by the position property.
   */
  public StandardEntity
      getPosition( WorldModel<? extends StandardEntity> model ) {
    if ( !position.isDefined() ) {
      return null;
    }
    return model.getEntity( position.getValue() );
  }


  /**
   * Set the position of this human.
   *
   * @param newPosition
   *          The new position.
   * @param newX
   *          The x coordinate of this agent.
   * @param newYStandardPropertyURN
   *          The y coordinate if this agent.
   */
  public void setPosition( EntityID newPosition, int newX, int newY ) {
    this.position.setValue( newPosition );
    this.x.setValue( newX );
    this.y.setValue( newY );
  }


  @Override
  public JSONObject toJson() {
    JSONObject jsonObject = super.toJson();
    jsonObject.put( StandardPropertyURN.DAMAGE.toString(),
        this.isDamageDefined() ? this.getDamage() : JSONObject.NULL );
    jsonObject.put( StandardPropertyURN.HP.toString(),
        this.isHPDefined() ? this.getHP() : JSONObject.NULL );
    jsonObject.put( StandardPropertyURN.POSITION.toString(),
        this.isPositionDefined() ? new int[]{ getX(), getY() }
            : JSONObject.NULL );
    jsonObject.put( StandardPropertyURN.POSITION_HISTORY.toString(),
        this.isPositionHistoryDefined() ? this.getPositionHistory()
            : JSONObject.NULL );

    return jsonObject;
  }
}
