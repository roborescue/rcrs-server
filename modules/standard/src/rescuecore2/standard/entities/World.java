package rescuecore2.standard.entities;

import java.util.List;
import java.util.Map;

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
   * @param id The ID of this entity.
   */
  public World(EntityID id) {
    super(id);
    this.startTime = new IntProperty(StandardPropertyURN.START_TIME);
    this.longitude = new IntProperty(StandardPropertyURN.LONGITUDE);
    this.latitude = new IntProperty(StandardPropertyURN.LATITUDE);
    this.windForce = new IntProperty(StandardPropertyURN.WIND_FORCE);
    this.windDirection = new IntProperty(StandardPropertyURN.WIND_DIRECTION);
    registerProperties(this.startTime, this.longitude, this.latitude, this.windForce, this.windDirection);
  }

  /**
   * World copy constructor.
   *
   * @param other The World to copy.
   */
  public World(World other) {
    super(other);
    this.startTime = new IntProperty(other.startTime);
    this.longitude = new IntProperty(other.longitude);
    this.latitude = new IntProperty(other.latitude);
    this.windForce = new IntProperty(other.windForce);
    this.windDirection = new IntProperty(other.windDirection);
    registerProperties(this.startTime, this.longitude, this.latitude, this.windForce, this.windDirection);
  }

  @Override
  protected Entity copyImpl() {
    return new World(getID());
  }

  @Override
  public StandardEntityURN getStandardURN() {
    return StandardEntityURN.WORLD;
  }

  @Override
  public Property<?> getProperty(String urn) {
    StandardPropertyURN type;
    try {
      type = StandardPropertyURN.fromString(urn);
    } catch (IllegalArgumentException e) {
      return super.getProperty(urn);
    }
    switch (type) {
      case START_TIME:
        return this.startTime;
      case LONGITUDE:
        return this.longitude;
      case LATITUDE:
        return this.latitude;
      case WIND_FORCE:
        return this.windForce;
      case WIND_DIRECTION:
        return this.windDirection;
      default:
        return super.getProperty(urn);
    }
  }

  /**
   * Get the startTime property.
   *
   * @return The startTime property.
   */
  public IntProperty getStartTimeProperty() {
    return this.startTime;
  }

  /**
   * Get the value of the startTime property.
   *
   * @return The value of the startTime property.
   */
  public int getStartTime() {
    return this.startTime.getValue();
  }

  /**
   * Set the startTime property.
   *
   * @param startTime The new startTime.
   */
  public void setStartTime(int startTime) {
    this.startTime.setValue(startTime);
  }

  /**
   * Find out if the startTime property has been defined.
   *
   * @return True if the startTime property has been defined, false otherwise.
   */
  public boolean isStartTimeDefined() {
    return this.startTime.isDefined();
  }

  /**
   * Undefine the startTime property.
   */
  public void undefineStartTime() {
    this.startTime.undefine();
  }

  /**
   * Get the latitude property.
   *
   * @return The latitude property.
   */
  public IntProperty getLatitudeProperty() {
    return this.latitude;
  }

  /**
   * Get the value of the latitude property.
   *
   * @return The value of the latitude property.
   */
  public int getLatitude() {
    return this.latitude.getValue();
  }

  /**
   * Set the latitude property.
   *
   * @param latitude The new latitude.
   */
  public void setLatitude(int latitude) {
    this.latitude.setValue(latitude);
  }

  /**
   * Find out if the latitude property has been defined.
   *
   * @return True if the latitude property has been defined, false otherwise.
   */
  public boolean isLatitudeDefined() {
    return this.latitude.isDefined();
  }

  /**
   * Undefine the latitude property.
   */
  public void undefineLatitude() {
    this.latitude.undefine();
  }

  /**
   * Get the longitude property.
   *
   * @return The longitude property.
   */
  public IntProperty getLongitudeProperty() {
    return this.longitude;
  }

  /**
   * Get the value of the longitude property.
   *
   * @return The value of the longitude property.
   */
  public int getLongitude() {
    return this.longitude.getValue();
  }

  /**
   * Set the longitude property.
   *
   * @param longitude The new longitude.
   */
  public void setLongitude(int longitude) {
    this.longitude.setValue(longitude);
  }

  /**
   * Find out if the longitude property has been defined.
   *
   * @return True if the longitude property has been defined, false otherwise.
   */
  public boolean isLongitudeDefined() {
    return this.longitude.isDefined();
  }

  /**
   * Undefine the longitude property.
   */
  public void undefineLongitude() {
    this.longitude.undefine();
  }

  /**
   * Get the windForce property.
   *
   * @return The windForce property.
   */
  public IntProperty getWindForceProperty() {
    return this.windForce;
  }

  /**
   * Get the value of the windForce property.
   *
   * @return The value of the windForce property.
   */
  public int getWindForce() {
    return this.windForce.getValue();
  }

  /**
   * Set the windForce property.
   *
   * @param windForce The new windForce.
   */
  public void setWindForce(int windForce) {
    this.windForce.setValue(windForce);
  }

  /**
   * Find out if the windForce property has been defined.
   *
   * @return True if the windForce property has been defined, false otherwise.
   */
  public boolean isWindForceDefined() {
    return this.windForce.isDefined();
  }

  /**
   * Undefine the windForce property.
   */
  public void undefineWindForce() {
    this.windForce.undefine();
  }

  /**
   * Get the windDirection property.
   *
   * @return The windDirection property.
   */
  public IntProperty getWindDirectionProperty() {
    return this.windDirection;
  }

  /**
   * Get the value of the windDirection property.
   *
   * @return The value of the windDirection property.
   */
  public int getWindDirection() {
    return this.windDirection.getValue();
  }

  /**
   * Set the windDirection property.
   *
   * @param windDirection The new windDirection.
   */
  public void setWindDirection(int windDirection) {
    this.windDirection.setValue(windDirection);
  }

  /**
   * Find out if the windDirection property has been defined.
   *
   * @return True if the windDirection property has been defined, false otherwise.
   */
  public boolean isWindDirectionDefined() {
    return this.windDirection.isDefined();
  }

  /**
   * Undefine the windDirection property.
   */
  public void undefineWindDirection() {
    this.windDirection.undefine();
  }

  @Override
  public void setEntity(Map<String, List<Object>> properties) {
    StandardPropertyURN type;

    for (String urn : properties.keySet()) {
      List<Object> fields = properties.get(urn);

      type = StandardPropertyURN.fromString(urn);
      switch (type) {
        case START_TIME:
          this.setStartTime(this.getStartTimeProperty().convertToValue(fields));
          break;
        case LONGITUDE:
          this.setLongitude(this.getLongitudeProperty().convertToValue(fields));
          break;
        case LATITUDE:
          this.setLatitude(this.getLatitudeProperty().convertToValue(fields));
          break;
        case WIND_FORCE:
          this.setWindForce(this.getWindForceProperty().convertToValue(fields));
          break;
        case WIND_DIRECTION:
          this.setWindDirection(this.getWindDirectionProperty().convertToValue(fields));
          break;
        default:
      }
    }
  }
}