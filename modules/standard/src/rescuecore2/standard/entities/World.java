package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;

/**
 * The World object.
 */
public class World extends StandardEntity {

  private IntProperty startTime;
  private IntProperty longitude;
  private IntProperty latitude;
  private IntProperty windForce;
  private IntProperty windDirection;


  /**
   * Construct a World object with entirely undefined property values.
   *
   * @param id
   *          The ID of this entity.
   */
  public World( EntityID id ) {
    super( id );
    startTime = new IntProperty( StandardPropertyURN.START_TIME );
    longitude = new IntProperty( StandardPropertyURN.LONGITUDE );
    latitude = new IntProperty( StandardPropertyURN.LATITUDE );
    windForce = new IntProperty( StandardPropertyURN.WIND_FORCE );
    windDirection = new IntProperty( StandardPropertyURN.WIND_DIRECTION );
    registerProperties( startTime, longitude, latitude, windForce,
        windDirection );
  }


  /**
   * World copy constructor.
   *
   * @param other
   *          The World to copy.
   */
  public World( World other ) {
    super( other );
    startTime = new IntProperty( other.startTime );
    longitude = new IntProperty( other.longitude );
    latitude = new IntProperty( other.latitude );
    windForce = new IntProperty( other.windForce );
    windDirection = new IntProperty( other.windDirection );
    registerProperties( startTime, longitude, latitude, windForce,
        windDirection );
  }


  @Override
  protected Entity copyImpl() {
    return new World( getID() );
  }


  @Override
  public StandardEntityURN getStandardURN() {
    return StandardEntityURN.WORLD;
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
      case START_TIME:
        return startTime;
      case LONGITUDE:
        return longitude;
      case LATITUDE:
        return latitude;
      case WIND_FORCE:
        return windForce;
      case WIND_DIRECTION:
        return windDirection;
      default:
        return super.getProperty( urn );
    }
  }


  /**
   * Get the startTime property.
   *
   * @return The startTime property.
   */
  public IntProperty getStartTimeProperty() {
    return startTime;
  }


  /**
   * Get the value of the startTime property.
   *
   * @return The value of the startTime property.
   */
  public int getStartTime() {
    return startTime.getValue();
  }


  /**
   * Set the startTime property.
   *
   * @param startTime
   *          The new startTime.
   */
  public void setStartTime( int startTime ) {
    this.startTime.setValue( startTime );
  }


  /**
   * Find out if the startTime property has been defined.
   *
   * @return True if the startTime property has been defined, false otherwise.
   */
  public boolean isStartTimeDefined() {
    return startTime.isDefined();
  }


  /**
   * Undefine the startTime property.
   */
  public void undefineStartTime() {
    startTime.undefine();
  }


  /**
   * Get the latitude property.
   *
   * @return The latitude property.
   */
  public IntProperty getLatitudeProperty() {
    return latitude;
  }


  /**
   * Get the value of the latitude property.
   *
   * @return The value of the latitude property.
   */
  public int getLatitude() {
    return latitude.getValue();
  }


  /**
   * Set the latitude property.
   *
   * @param latitude
   *          The new latitude.
   */
  public void setLatitude( int latitude ) {
    this.latitude.setValue( latitude );
  }


  /**
   * Find out if the latitude property has been defined.
   *
   * @return True if the latitude property has been defined, false otherwise.
   */
  public boolean isLatitudeDefined() {
    return latitude.isDefined();
  }


  /**
   * Undefine the latitude property.
   */
  public void undefineLatitude() {
    latitude.undefine();
  }


  /**
   * Get the longitude property.
   *
   * @return The longitude property.
   */
  public IntProperty getLongitudeProperty() {
    return longitude;
  }


  /**
   * Get the value of the longitude property.
   *
   * @return The value of the longitude property.
   */
  public int getLongitude() {
    return longitude.getValue();
  }


  /**
   * Set the longitude property.
   *
   * @param longitude
   *          The new longitude.
   */
  public void setLongitude( int longitude ) {
    this.longitude.setValue( longitude );
  }


  /**
   * Find out if the longitude property has been defined.
   *
   * @return True if the longitude property has been defined, false otherwise.
   */
  public boolean isLongitudeDefined() {
    return longitude.isDefined();
  }


  /**
   * Undefine the longitude property.
   */
  public void undefineLongitude() {
    longitude.undefine();
  }


  /**
   * Get the windForce property.
   *
   * @return The windForce property.
   */
  public IntProperty getWindForceProperty() {
    return windForce;
  }


  /**
   * Get the value of the windForce property.
   *
   * @return The value of the windForce property.
   */
  public int getWindForce() {
    return windForce.getValue();
  }


  /**
   * Set the windForce property.
   *
   * @param windForce
   *          The new windForce.
   */
  public void setWindForce( int windForce ) {
    this.windForce.setValue( windForce );
  }


  /**
   * Find out if the windForce property has been defined.
   *
   * @return True if the windForce property has been defined, false otherwise.
   */
  public boolean isWindForceDefined() {
    return windForce.isDefined();
  }


  /**
   * Undefine the windForce property.
   */
  public void undefineWindForce() {
    windForce.undefine();
  }


  /**
   * Get the windDirection property.
   *
   * @return The windDirection property.
   */
  public IntProperty getWindDirectionProperty() {
    return windDirection;
  }


  /**
   * Get the value of the windDirection property.
   *
   * @return The value of the windDirection property.
   */
  public int getWindDirection() {
    return windDirection.getValue();
  }


  /**
   * Set the windDirection property.
   *
   * @param windDirection
   *          The new windDirection.
   */
  public void setWindDirection( int windDirection ) {
    this.windDirection.setValue( windDirection );
  }


  /**
   * Find out if the windDirection property has been defined.
   *
   * @return True if the windDirection property has been defined, false
   *         otherwise.
   */
  public boolean isWindDirectionDefined() {
    return windDirection.isDefined();
  }


  /**
   * Undefine the windDirection property.
   */
  public void undefineWindDirection() {
    windDirection.undefine();
  }
}
