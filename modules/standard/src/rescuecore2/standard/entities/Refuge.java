package rescuecore2.standard.entities;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;

/**
 * The Refuge object.
 */
/*
 * Implementation of Refuge Bed Capacity
 *
 * @author Farshid Faraji May 2020 During Covid-19 :-)))
 */
public class Refuge extends Building {

  private IntProperty bedCapacity;
  private IntProperty occupiedBeds;
  private IntProperty refillCapacity;
  private IntProperty waitingListSize;

  /**
   * Construct a Refuge object with entirely undefined values.
   *
   * @param id The ID of this entity.
   */
  public Refuge(EntityID id) {
    super(id);
    this.bedCapacity = new IntProperty(StandardPropertyURN.BEDCAPACITY);
    this.occupiedBeds = new IntProperty(StandardPropertyURN.OCCUPIEDBEDS);
    this.refillCapacity = new IntProperty(StandardPropertyURN.REFILLCAPACITY);
    this.waitingListSize = new IntProperty(StandardPropertyURN.WAITINGLISTSIZE);
    registerProperties(this.bedCapacity, this.occupiedBeds, this.refillCapacity, this.waitingListSize);
  }

  /**
   * Refuge copy constructor.
   *
   * @param other The Refuge to copy.
   */
  public Refuge(Refuge other) {
    super(other);
    this.bedCapacity = new IntProperty(StandardPropertyURN.BEDCAPACITY);
    this.occupiedBeds = new IntProperty(StandardPropertyURN.OCCUPIEDBEDS);
    this.refillCapacity = new IntProperty(StandardPropertyURN.REFILLCAPACITY);
    this.waitingListSize = new IntProperty(StandardPropertyURN.WAITINGLISTSIZE);
    registerProperties(this.bedCapacity, this.occupiedBeds, this.refillCapacity, this.waitingListSize);
  }

  /**
   * Create a refuge based on another Building.
   *
   * @param other The Building to copy.
   */
  public Refuge(Building other) {
    super(other);
    this.bedCapacity = new IntProperty(StandardPropertyURN.BEDCAPACITY);
    this.occupiedBeds = new IntProperty(StandardPropertyURN.OCCUPIEDBEDS);
    this.refillCapacity = new IntProperty(StandardPropertyURN.REFILLCAPACITY);
    this.waitingListSize = new IntProperty(StandardPropertyURN.WAITINGLISTSIZE);
    registerProperties(this.bedCapacity, this.occupiedBeds, this.refillCapacity, this.waitingListSize);
  }

  @Override
  protected Entity copyImpl() {
    return new Refuge(getID());
  }

  @Override
  public StandardEntityURN getStandardURN() {
    return StandardEntityURN.REFUGE;
  }

  @Override
  protected String getEntityName() {
    return "Refuge";
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
      case BEDCAPACITY:
        return this.bedCapacity;
      case OCCUPIEDBEDS:
        return this.occupiedBeds;
      case REFILLCAPACITY:
        return this.refillCapacity;
      case WAITINGLISTSIZE:
        return this.waitingListSize;
      default:
        return super.getProperty(urn);
    }
  }

  /**
   * Get the occupiedBeds property.
   *
   * @return The occupiedBeds property.
   */
  public IntProperty getOccupiedBedsProperty() {
    return this.occupiedBeds;
  }

  /**
   * Get the occupiedBeds of this refuge.
   *
   * @return The occupiedBeds.
   */
  public int getOccupiedBeds() {
    return this.occupiedBeds.getValue();
  }

  /**
   * Set the occupiedBeds of this refuge.
   *
   * @param capacity The new occupiedBeds.
   */
  public void setOccupiedBeds(int capacity) {
    this.occupiedBeds.setValue(capacity);
  }

  /**
   * Increase the occupiedBeds of this refuge by one.
   */
  public int increaseOccupiedBeds() {
    if (this.occupiedBeds.getValue() < this.bedCapacity.getValue()) {
      this.occupiedBeds.setValue(this.occupiedBeds.getValue() + 1);
    }
    return occupiedBeds.getValue();
  }

  /**
   * Decrease the occupiedBeds of this refuge by one.
   */
  public int decreaseOccupiedBeds() {
    if (this.occupiedBeds.getValue() > 0) {
      this.occupiedBeds.setValue(this.occupiedBeds.getValue() - 1);
    }
    return this.occupiedBeds.getValue();
  }

  /**
   * Find out if the occupiedBeds property has been defined.
   *
   * @return True if the occupiedBeds property has been defined, false otherwise.
   */
  public boolean isOccupiedBedsDefined() {
    return this.occupiedBeds.isDefined();
  }

  /**
   * Undefine the occupiedBeds property.
   */
  public void undefineOccupiedBeds() {
    this.occupiedBeds.undefine();
  }

  /**
   * Get the bedCapacity property.
   *
   * @return The bedCapacity property.
   */
  public IntProperty getBedCapacityProperty() {
    return this.bedCapacity;
  }

  /**
   * Get the bedCapacity of this refuge.
   *
   * @return The bedCapacity.
   */
  public int getBedCapacity() {
    return this.bedCapacity.getValue();
  }

  /**
   * Set the bedCapacity of this refuge.
   *
   * @param this.capacity The new bedCapacity.
   */
  public void setBedCapacity(int capacity) {
    // if((Object) capacity != null)
    this.bedCapacity.setValue(capacity);
  }

  /**
   * Find out if the bedCapacity property has been defined.
   *
   * @return True if the bedCapacity property has been defined, false otherwise.
   */
  public boolean isBedCapacityDefined() {
    return this.bedCapacity.isDefined();
  }

  /**
   * Undefine the bedCapacity property.
   */
  public void undefineBedCapacity() {
    this.bedCapacity.undefine();
  }

  /**
   * Get the refillCapacity property.
   *
   * @return The refillCapacity property.
   */
  public IntProperty getRefillCapacityProperty() {
    return this.refillCapacity;
  }

  /**
   * Get the refillCapacity of this refuge.
   *
   * @return The refillCapacity.
   */
  public int getRefillCapacity() {
    return this.refillCapacity.getValue();
  }

  /**
   * Set the refillCapacity of this refuge.
   *
   * @param capacity The new refillCapacity.
   */
  public void setRefillCapacity(int capacity) {
    this.refillCapacity.setValue(capacity);
  }

  /**
   * Find out if the refillCapacity property has been defined.
   *
   * @return True if the refillCapacity property has been defined, false
   *         otherwise.
   */
  public boolean isRefillCapacityDefined() {
    return this.refillCapacity.isDefined();
  }

  /**
   * Undefine the refillCapacity property.
   */
  public void undefineRefillCapacity() {
    this.refillCapacity.undefine();
  }

  /**
   * Get the waitingListSize property.
   *
   * @return The waitingListSize property.
   */
  public IntProperty getWaitingListSizeProperty() {
    return this.waitingListSize;
  }

  /**
   * Get the waitingListSize of this refuge.
   *
   * @return The waitingListSize.
   */
  public int getWaitingListSize() {
    return this.waitingListSize.getValue();
  }

  /**
   * Set the waitingListSize of this refuge.
   *
   * @param size The new waitingList.
   */
  public void setWaitingListSize(int size) {
    this.waitingListSize.setValue(size);
  }

  /**
   * Find out if the waitingListSize property has been defined.
   *
   * @return True if the waitingListSize property has been defined, false
   *         otherwise.
   */
  public boolean isWaitingListSizeDefined() {
    return this.waitingListSize.isDefined();
  }

  /**
   * Undefine the waitingListSize property.
   */
  public void undefineWaitingListSize() {
    this.waitingListSize.undefine();
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
        case BEDCAPACITY:
          this.setBedCapacity(this.getBedCapacityProperty().convertToValue(fields));
          break;
        case OCCUPIEDBEDS:
          this.setOccupiedBeds(this.getOccupiedBedsProperty().convertToValue(fields));
          break;
        case REFILLCAPACITY:
          this.setRefillCapacity(this.getRefillCapacityProperty().convertToValue(fields));
          break;
        case WAITINGLISTSIZE:
          this.setWaitingListSize(this.getWaitingListSizeProperty().convertToValue(fields));
          break;
        default:
      }
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject jsonObject = super.toJson();
    jsonObject.put(StandardPropertyURN.BEDCAPACITY.toString(),
        this.isBedCapacityDefined() ? this.getBedCapacity() : JSONObject.NULL);
    jsonObject.put(StandardPropertyURN.OCCUPIEDBEDS.toString(),
        this.isOccupiedBedsDefined() ? this.getOccupiedBeds() : JSONObject.NULL);
    jsonObject.put(StandardPropertyURN.REFILLCAPACITY.toString(),
        this.isRefillCapacityDefined() ? this.getRefillCapacity() : JSONObject.NULL);
    jsonObject.put(StandardPropertyURN.WAITINGLISTSIZE.toString(),
        this.isWaitingListSizeDefined() ? this.getWaitingListSize() : JSONObject.NULL);

    return jsonObject;
  }
}