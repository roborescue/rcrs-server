package rescuecore2.standard.entities;

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
    bedCapacity = new IntProperty(StandardPropertyURN.BED_CAPACITY);
    occupiedBeds = new IntProperty(StandardPropertyURN.OCCUPIED_BEDS);
    refillCapacity = new IntProperty(StandardPropertyURN.REFILL_CAPACITY);
    waitingListSize = new IntProperty(StandardPropertyURN.WAITING_LIST_SIZE);
    registerProperties(bedCapacity, occupiedBeds, refillCapacity, waitingListSize);
  }

  /**
   * Refuge copy constructor.
   *
   * @param other The Refuge to copy.
   */
  public Refuge(Refuge other) {
    super(other);
    bedCapacity = new IntProperty(StandardPropertyURN.BED_CAPACITY);
    occupiedBeds = new IntProperty(StandardPropertyURN.OCCUPIED_BEDS);
    refillCapacity = new IntProperty(StandardPropertyURN.REFILL_CAPACITY);
    waitingListSize = new IntProperty(StandardPropertyURN.WAITING_LIST_SIZE);
    registerProperties(bedCapacity, occupiedBeds, refillCapacity, waitingListSize);
  }

  /**
   * Create a refuge based on another Building.
   *
   * @param other The Building to copy.
   */
  public Refuge(Building other) {
    super(other);
    bedCapacity = new IntProperty(StandardPropertyURN.BED_CAPACITY);
    occupiedBeds = new IntProperty(StandardPropertyURN.OCCUPIED_BEDS);
    refillCapacity = new IntProperty(StandardPropertyURN.REFILL_CAPACITY);
    waitingListSize = new IntProperty(StandardPropertyURN.WAITING_LIST_SIZE);
    registerProperties(bedCapacity, occupiedBeds, refillCapacity, waitingListSize);
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
  public Property getProperty(int urn) {
    StandardPropertyURN type;
    try {
      type = StandardPropertyURN.fromInt(urn);
    } catch (IllegalArgumentException e) {
      return super.getProperty(urn);
    }
    switch (type) {
      case BED_CAPACITY:
        return bedCapacity;
      case OCCUPIED_BEDS:
        return occupiedBeds;
      case REFILL_CAPACITY:
        return refillCapacity;
      case WAITING_LIST_SIZE:
        return waitingListSize;
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
    return occupiedBeds;
  }

  /**
   * Get the occupiedBeds of this refuge.
   *
   * @return The occupiedBeds.
   */
  public int getOccupiedBeds() {
    return occupiedBeds.getValue();
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
    if (occupiedBeds.getValue() < bedCapacity.getValue()) {
      occupiedBeds.setValue(occupiedBeds.getValue() + 1);
    }
    return occupiedBeds.getValue();
  }

  /**
   * Decrease the occupiedBeds of this refuge by one.
   */
  public int decreaseOccupiedBeds() {
    if (occupiedBeds.getValue() > 0) {
      occupiedBeds.setValue(occupiedBeds.getValue() - 1);
    }
    return occupiedBeds.getValue();
  }

  /**
   * Find out if the occupiedBeds property has been defined.
   *
   * @return True if the occupiedBeds property has been defined, false otherwise.
   */
  public boolean isOccupiedBedsDefined() {
    return occupiedBeds.isDefined();
  }

  /**
   * Undefine the occupiedBeds property.
   */
  public void undefineOccupiedBeds() {
    occupiedBeds.undefine();
  }

  /**
   * Get the bedCapacity property.
   *
   * @return The bedCapacity property.
   */
  public IntProperty getBedCapacityProperty() {
    return bedCapacity;
  }

  /**
   * Get the bedCapacity of this refuge.
   *
   * @return The bedCapacity.
   */
  public int getBedCapacity() {
    return bedCapacity.getValue();
  }

  /**
   * Set the bedCapacity of this refuge.
   *
   * @param capacity The new bedCapacity.
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
    return bedCapacity.isDefined();
  }

  /**
   * Undefine the bedCapacity property.
   */
  public void undefineBedCapacity() {
    bedCapacity.undefine();
  }

  /**
   * Get the refillCapacity property.
   *
   * @return The refillCapacity property.
   */
  public IntProperty getRefillCapacityProperty() {
    return refillCapacity;
  }

  /**
   * Get the refillCapacity of this refuge.
   *
   * @return The refillCapacity.
   */
  public int getRefillCapacity() {
    return refillCapacity.getValue();
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
    return refillCapacity.isDefined();
  }

  /**
   * Undefine the refillCapacity property.
   */
  public void undefineRefillCapacity() {
    refillCapacity.undefine();
  }

  /**
   * Get the waitingListSize property.
   *
   * @return The waitingListSize property.
   */
  public IntProperty getWaitingListSizeProperty() {
    return waitingListSize;
  }

  /**
   * Get the waitingListSize of this refuge.
   *
   * @return The waitingListSize.
   */
  public int getWaitingListSize() {
    return waitingListSize.getValue();
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
    return waitingListSize.isDefined();
  }

  /**
   * Undefine the waitingListSize property.
   */
  public void undefineWaitingListSize() {
    waitingListSize.undefine();
  }
}
