package rescuecore2.standard.entities;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.BooleanProperty;
import rescuecore2.worldmodel.properties.IntProperty;

/**
 * The Building object.
 */
public class Building extends Area {

  /**
   * Fieryness levels that indicate burning.
   */
  public static final EnumSet<StandardEntityConstants.Fieryness> BURNING = EnumSet.of(
      StandardEntityConstants.Fieryness.HEATING, StandardEntityConstants.Fieryness.BURNING,
      StandardEntityConstants.Fieryness.INFERNO);

  private IntProperty floors;
  private BooleanProperty ignition;
  private IntProperty fieryness;
  private IntProperty brokenness;
  private IntProperty code;
  private IntProperty attributes;
  private IntProperty groundArea;
  private IntProperty totalArea;
  private IntProperty temperature;
  private IntProperty importance;
  private IntProperty capacity;

  /**
   * Construct a Building object with entirely undefined property values.
   *
   * @param id The ID of this entity.
   */
  public Building(EntityID id) {
    super(id);
    this.floors = new IntProperty(StandardPropertyURN.FLOORS);
    this.ignition = new BooleanProperty(StandardPropertyURN.IGNITION);
    this.fieryness = new IntProperty(StandardPropertyURN.FIERYNESS);
    this.brokenness = new IntProperty(StandardPropertyURN.BROKENNESS);
    this.code = new IntProperty(StandardPropertyURN.BUILDING_CODE);
    this.attributes = new IntProperty(StandardPropertyURN.BUILDING_ATTRIBUTES);
    this.groundArea = new IntProperty(StandardPropertyURN.BUILDING_AREA_GROUND);
    this.totalArea = new IntProperty(StandardPropertyURN.BUILDING_AREA_TOTAL);
    this.temperature = new IntProperty(StandardPropertyURN.TEMPERATURE);
    this.importance = new IntProperty(StandardPropertyURN.IMPORTANCE);
    this.capacity = new IntProperty(StandardPropertyURN.CAPACITY);
    registerProperties(this.floors, this.ignition, this.fieryness, this.brokenness, this.code, this.attributes,
        this.groundArea, this.totalArea, this.temperature, this.importance, this.capacity);
  }

  /**
   * Building copy constructor.
   *
   * @param other The Building to copy.
   */
  public Building(Building other) {
    super(other);
    this.floors = new IntProperty(other.floors);
    this.ignition = new BooleanProperty(other.ignition);
    this.fieryness = new IntProperty(other.fieryness);
    this.brokenness = new IntProperty(other.brokenness);
    this.code = new IntProperty(other.code);
    this.attributes = new IntProperty(other.attributes);
    this.groundArea = new IntProperty(other.groundArea);
    this.totalArea = new IntProperty(other.totalArea);
    this.temperature = new IntProperty(other.temperature);
    this.importance = new IntProperty(other.importance);
    this.capacity = new IntProperty(StandardPropertyURN.CAPACITY);
    registerProperties(this.floors, this.ignition, this.fieryness, this.brokenness, this.code, this.attributes,
        this.groundArea, this.totalArea, this.temperature, this.importance, this.capacity);
  }

  @Override
  protected Entity copyImpl() {
    return new Building(getID());
  }

  @Override
  public StandardEntityURN getStandardURN() {
    return StandardEntityURN.BUILDING;
  }

  @Override
  protected String getEntityName() {
    return "Building";
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
      case FLOORS:
        return this.floors;
      case IGNITION:
        return this.ignition;
      case FIERYNESS:
        return this.fieryness;
      case BROKENNESS:
        return this.brokenness;
      case BUILDING_CODE:
        return this.code;
      case BUILDING_ATTRIBUTES:
        return this.attributes;
      case BUILDING_AREA_GROUND:
        return this.groundArea;
      case BUILDING_AREA_TOTAL:
        return this.totalArea;
      case TEMPERATURE:
        return this.temperature;
      case IMPORTANCE:
        return this.importance;
      case CAPACITY:
        return this.capacity;
      default:
        return super.getProperty(urn);
    }
  }

  /**
   * Get the floors property.
   *
   * @return The floors property.
   */
  public IntProperty getFloorsProperty() {
    return this.floors;
  }

  /**
   * Get the number of floors in this building.
   *
   * @return The number of floors.
   */
  public int getFloors() {
    return this.floors.getValue();
  }

  /**
   * Set the number of floors in this building.
   *
   * @param floors The new number of floors.
   */
  public void setFloors(int floors) {
    this.floors.setValue(floors);
  }

  /**
   * Find out if the floors property has been defined.
   *
   * @return True if the floors property has been defined, false otherwise.
   */
  public boolean isFloorsDefined() {
    return this.floors.isDefined();
  }

  /**
   * Undefine the floors property.
   */
  public void undefineFloors() {
    this.floors.undefine();
  }

  /**
   * Get the ignition property.
   *
   * @return The ignition property.
   */
  public BooleanProperty getIgnitionProperty() {
    return this.ignition;
  }

  /**
   * Get the value of the ignition property.
   *
   * @return The ignition property value.
   */
  public boolean getIgnition() {
    return this.ignition.getValue();
  }

  /**
   * Set the ignition property.
   *
   * @param ignition The new ignition value.
   */
  public void setIgnition(boolean ignition) {
    this.ignition.setValue(ignition);
  }

  /**
   * Find out if the ignition property has been defined.
   *
   * @return True if the ignition property has been defined, false otherwise.
   */
  public boolean isIgnitionDefined() {
    return this.ignition.isDefined();
  }

  /**
   * Undefine the ignition property.
   */
  public void undefineIgnition() {
    this.ignition.undefine();
  }

  /**
   * Get the fieryness property.
   *
   * @return The fieryness property.
   */
  public IntProperty getFierynessProperty() {
    return this.fieryness;
  }

  /**
   * Get the fieryness of this building.
   *
   * @return The fieryness property value.
   */
  public int getFieryness() {
    return this.fieryness.getValue();
  }

  /**
   * Get the fieryness of this building as an enum constant. If fieryness is not
   * defined then return null.
   *
   * @return The fieryness property value as a Fieryness enum, or null if
   *         fieryness is undefined.
   */
  public StandardEntityConstants.Fieryness getFierynessEnum() {
    if (!this.fieryness.isDefined()) {
      return null;
    }
    return StandardEntityConstants.Fieryness.values()[this.fieryness.getValue()];
  }

  /**
   * Set the fieryness of this building.
   *
   * @param fieryness The new fieryness value.
   */
  public void setFieryness(int fieryness) {
    this.fieryness.setValue(fieryness);
  }

  /**
   * Find out if the fieryness property has been defined.
   *
   * @return True if the fieryness property has been defined, false otherwise.
   */
  public boolean isFierynessDefined() {
    return this.fieryness.isDefined();
  }

  /**
   * Undefine the fieryness property.
   */
  public void undefineFieryness() {
    this.fieryness.undefine();
  }

  /**
   * Get the brokenness property.
   *
   * @return The brokenness property.
   */
  public IntProperty getBrokennessProperty() {
    return this.brokenness;
  }

  /**
   * Get the brokenness of this building.
   *
   * @return The brokenness value.
   */
  public int getBrokenness() {
    return this.brokenness.getValue();
  }

  /**
   * Set the brokenness of this building.
   *
   * @param brokenness The new brokenness.
   */
  public void setBrokenness(int brokenness) {
    this.brokenness.setValue(brokenness);
  }

  /**
   * Find out if the brokenness property has been defined.
   *
   * @return True if the brokenness property has been defined, false otherwise.
   */
  public boolean isBrokennessDefined() {
    return this.brokenness.isDefined();
  }

  /**
   * Undefine the brokenness property.
   */
  public void undefineBrokenness() {
    this.brokenness.undefine();
  }

  /**
   * Get the building code property.
   *
   * @return The building code property.
   */
  public IntProperty getBuildingCodeProperty() {
    return this.code;
  }

  /**
   * Get the building code of this building.
   *
   * @return The building code.
   */
  public int getBuildingCode() {
    return this.code.getValue();
  }

  /**
   * Get the building code of this building as an enum constant. If building code
   * is not defined then return null.
   *
   * @return The building code property value as a BuildingCode enum, or null if
   *         building code is undefined.
   */
  public StandardEntityConstants.BuildingCode getBuildingCodeEnum() {
    if (!this.code.isDefined()) {
      return null;
    }
    return StandardEntityConstants.BuildingCode.values()[this.code.getValue()];
  }

  /**
   * Set the building code of this building.
   *
   * @param newCode The new building code.
   */
  public void setBuildingCode(int code) {
    this.code.setValue(code);
  }

  /**
   * Find out if the building code has been defined.
   *
   * @return True if the building code has been defined, false otherwise.
   */
  public boolean isBuildingCodeDefined() {
    return this.code.isDefined();
  }

  /**
   * Undefine the building code.
   */
  public void undefineBuildingCode() {
    this.code.undefine();
  }

  /**
   * Get the building attributes property.
   *
   * @return The building attributes property.
   */
  public IntProperty getBuildingAttributesProperty() {
    return this.attributes;
  }

  /**
   * Get the building attributes of this building.
   *
   * @return The building attributes.
   */
  public int getBuildingAttributes() {
    return this.attributes.getValue();
  }

  /**
   * Set the building attributes of this building.
   *
   * @param newAttributes The new building attributes.
   */
  public void setBuildingAttributes(int attributes) {
    this.attributes.setValue(attributes);
  }

  /**
   * Find out if the building attributes property has been defined.
   *
   * @return True if the building attributes property has been defined, false
   *         otherwise.
   */
  public boolean isBuildingAttributesDefined() {
    return this.attributes.isDefined();
  }

  /**
   * Undefine the building attributes.
   */
  public void undefineBuildingAttributes() {
    this.attributes.undefine();
  }

  /**
   * Get the ground area property.
   *
   * @return The ground area property.
   */
  public IntProperty getGroundAreaProperty() {
    return this.groundArea;
  }

  /**
   * Get the ground area of this building.
   *
   * @return The ground area.
   */
  public int getGroundArea() {
    return this.groundArea.getValue();
  }

  /**
   * Set the ground area of this building.
   *
   * @param ground The new ground area.
   */
  public void setGroundArea(int ground) {
    this.groundArea.setValue(ground);
  }

  /**
   * Find out if the ground area property has been defined.
   *
   * @return True if the ground area property has been defined, false otherwise.
   */
  public boolean isGroundAreaDefined() {
    return this.groundArea.isDefined();
  }

  /**
   * Undefine the ground area.
   */
  public void undefineGroundArea() {
    this.groundArea.undefine();
  }

  /**
   * Get the total area property.
   *
   * @return The total area property.
   */
  public IntProperty getTotalAreaProperty() {
    return this.totalArea;
  }

  /**
   * Get the total area of this building.
   *
   * @return The total area.
   */
  public int getTotalArea() {
    return this.totalArea.getValue();
  }

  /**
   * Set the total area of this building.
   *
   * @param total The new total area.
   */
  public void setTotalArea(int total) {
    this.totalArea.setValue(total);
  }

  /**
   * Find out if the total area property has been defined.
   *
   * @return True if the total area property has been defined, false otherwise.
   */
  public boolean isTotalAreaDefined() {
    return this.totalArea.isDefined();
  }

  /**
   * Undefine the total area property.
   */
  public void undefineTotalArea() {
    this.totalArea.undefine();
  }

  /**
   * Get the temperature property.
   *
   * @return The temperature property.
   */
  public IntProperty getTemperatureProperty() {
    return this.temperature;
  }

  /**
   * Get the temperature of this building.
   *
   * @return The temperature.
   */
  public int getTemperature() {
    return this.temperature.getValue();
  }

  /**
   * Set the temperature of this building.
   *
   * @param temperature The new temperature.
   */
  public void setTemperature(int temperature) {
    this.temperature.setValue(temperature);
  }

  /**
   * Find out if the temperature property has been defined.
   *
   * @return True if the temperature property has been defined, false otherwise.
   */
  public boolean isTemperatureDefined() {
    return this.temperature.isDefined();
  }

  /**
   * Undefine the temperature property.
   */
  public void undefineTemperature() {
    this.temperature.undefine();
  }

  /**
   * Get the building importance property.
   *
   * @return The importance property.
   */
  public IntProperty getImportanceProperty() {
    return this.importance;
  }

  /**
   * Get the importance of this building.
   *
   * @return The importance.
   */
  public int getImportance() {
    return this.importance.getValue();
  }

  /**
   * Set the importance of this building.
   *
   * @param importance The new importance.
   */
  public void setImportance(int importance) {
    this.importance.setValue(importance);
  }

  /**
   * Find out if the importance property has been defined.
   *
   * @return True if the importance property has been defined, false otherwise.
   */
  public boolean isImportanceDefined() {
    return this.importance.isDefined();
  }

  /**
   * Undefine the importance property.
   */
  public void undefineImportance() {
    this.importance.undefine();
  }

  /**
   * Get the capacity property.
   *
   * @return The capacity property.
   */
  public IntProperty getCapacityProperty() {
    return this.capacity;
  }

  /**
   * Get the capacity of this building.
   *
   * @return The capacity.
   */
  public int getCapacity() {
    return this.capacity.getValue();
  }

  /**
   * Set the capacity of this building.
   *
   * @param capacity The capacity.
   */
  public void setCapacity(int capacity) {
    this.capacity.setValue(capacity);
  }

  /**
   * Find out if the capacity property has been defined.
   *
   * @return True if the capacity property has been defined, false otherwise.
   */
  public boolean isCapacityDefined() {
    return this.capacity.isDefined();
  }

  /**
   * Undefine the capacity property.
   */
  public void undefineCapacity() {
    this.capacity.undefine();
  }

  /**
   * Find out if this building is on fire.
   *
   * @return True iff this buildings fieryness indicates that it is burning.
   */
  public boolean isOnFire() {
    if (!this.fieryness.isDefined()) {
      return false;
    }
    return BURNING.contains(getFierynessEnum());
  }

  @Override
  public void setEntity(Map<String, List<Object>> properties) {
    StandardPropertyURN type;

    for (String urn : properties.keySet()) {
      List<Object> fields = properties.get(urn);

      type = StandardPropertyURN.fromString(urn);
      switch (type) {
        case X:
          this.setX(this.getXProperty().convertToValue(fields));
          break;
        case Y:
          this.setY(this.getYProperty().convertToValue(fields));
          break;
        case EDGES:
          this.setEdges(this.getEdgesProperty().convertToValue(fields));
          break;
        case BLOCKADES:
          this.setBlockades(this.getBlockadesProperty().convertToValue(fields));
          break;
        case FLOORS:
          this.floors.setValue(this.floors.convertToValue(fields));
          break;
        case IGNITION:
          this.setIgnition(this.ignition.convertToValue(fields));
          break;
        case FIERYNESS:
          this.setFieryness(this.fieryness.convertToValue(fields));
          break;
        case BROKENNESS:
          this.setBrokenness(this.brokenness.convertToValue(fields));
          break;
        case BUILDING_CODE:
          this.setBuildingCode(this.code.convertToValue(fields));
          break;
        case BUILDING_ATTRIBUTES:
          this.setBuildingAttributes(this.attributes.convertToValue(fields));
          break;
        case BUILDING_AREA_GROUND:
          this.setGroundArea(this.groundArea.convertToValue(fields));
          break;
        case BUILDING_AREA_TOTAL:
          this.setTotalArea(this.totalArea.convertToValue(fields));
          break;
        case TEMPERATURE:
          this.setTemperature(this.temperature.convertToValue(fields));
          break;
        case IMPORTANCE:
          this.setImportance(this.importance.convertToValue(fields));
          break;
        default:
      }
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject jsonObject = super.toJson();
    jsonObject.put(StandardPropertyURN.BROKENNESS.toString(),
        this.isBrokennessDefined() ? this.getBrokenness() : JSONObject.NULL);
    jsonObject.put(StandardPropertyURN.FIERYNESS.toString(),
        this.isFierynessDefined() ? this.getFieryness() : JSONObject.NULL);
    jsonObject.put(StandardPropertyURN.FLOORS.toString(), this.isFloorsDefined() ? this.getFloors() : JSONObject.NULL);

    return jsonObject;
  }
}