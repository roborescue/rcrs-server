package rescuecore2.standard.entities;

import java.awt.Polygon;
import java.awt.Shape;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityListener;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.IntProperty;

/**
 * A blockade.
 */
public class Blockade extends StandardEntity {

  private IntProperty x;
  private IntProperty y;
  private EntityRefProperty position;
  private IntArrayProperty apexes;
  private IntProperty repairCost;

  private Shape shape;

  /**
   * Construct a Blockade object with entirely undefined property values.
   *
   * @param id The ID of this entity.
   */
  public Blockade(EntityID id) {
    super(id);
    this.x = new IntProperty(StandardPropertyURN.X);
    this.y = new IntProperty(StandardPropertyURN.Y);
    this.position = new EntityRefProperty(StandardPropertyURN.POSITION);
    this.apexes = new IntArrayProperty(StandardPropertyURN.APEXES);
    this.repairCost = new IntProperty(StandardPropertyURN.REPAIR_COST);
    registerProperties(this.x, this.y, this.position, this.apexes, this.repairCost);
    this.shape = null;
    addEntityListener(new ApexesListener());
  }

  /**
   * Blockade copy constructor.
   *
   * @param other The Blockade to copy.
   */
  public Blockade(Blockade other) {
    super(other);
    this.x = new IntProperty(other.x);
    this.y = new IntProperty(other.y);
    this.position = new EntityRefProperty(other.position);
    this.apexes = new IntArrayProperty(other.apexes);
    this.repairCost = new IntProperty(other.repairCost);
    registerProperties(this.x, this.y, this.position, this.apexes, this.repairCost);
    this.shape = null;
    addEntityListener(new ApexesListener());
  }

  @Override
  public Pair<Integer, Integer> getLocation(WorldModel<? extends StandardEntity> world) {
    if (!this.x.isDefined() || !this.y.isDefined()) {
      return null;
    }
    return new Pair<Integer, Integer>(this.x.getValue(), this.y.getValue());
  }

  @Override
  protected Entity copyImpl() {
    return new Blockade(getID());
  }

  @Override
  public StandardEntityURN getStandardURN() {
    return StandardEntityURN.BLOCKADE;
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
      case X:
        return this.x;
      case Y:
        return this.y;
      case POSITION:
        return this.position;
      case APEXES:
        return this.apexes;
      case REPAIR_COST:
        return this.repairCost;
      default:
        return super.getProperty(urn);
    }
  }

  /**
   * Get the X property.
   *
   * @return The X property.
   */
  public IntProperty getXProperty() {
    return this.x;
  }

  /**
   * Get the X coordinate.
   *
   * @return The X coordinate.
   */
  public int getX() {
    return this.x.getValue();
  }

  /**
   * Set the X coordinate.
   *
   * @param x The new X coordinate.
   */
  public void setX(int x) {
    this.x.setValue(x);
  }

  /**
   * Find out if the X property has been defined.
   *
   * @return True if the X property has been defined, false otherwise.
   */
  public boolean isXDefined() {
    return this.x.isDefined();
  }

  /**
   * Undefine the X property.
   */
  public void undefineX() {
    this.x.undefine();
  }

  /**
   * Get the Y property.
   *
   * @return The Y property.
   */
  public IntProperty getYProperty() {
    return this.y;
  }

  /**
   * Get the Y coordinate.
   *
   * @return The Y coordinate.
   */
  public int getY() {
    return this.y.getValue();
  }

  /**
   * Set the Y coordinate.
   *
   * @param y The new y coordinate.
   */
  public void setY(int y) {
    this.y.setValue(y);
  }

  /**
   * Find out if the Y property has been defined.
   *
   * @return True if the Y property has been defined, false otherwise.
   */
  public boolean isYDefined() {
    return this.y.isDefined();
  }

  /**
   * Undefine the Y property.
   */
  public void undefineY() {
    this.y.undefine();
  }

  /**
   * Get the apexes property.
   *
   * @return The apexes property.
   */
  public IntArrayProperty getApexesProperty() {
    return this.apexes;
  }

  /**
   * Get the apexes of this area.
   *
   * @return The apexes.
   */
  public int[] getApexes() {
    return this.apexes.getValue().stream().mapToInt(i -> i).toArray();
  }

  /**
   * Set the apexes.
   *
   * @param apexes The new apexes.
   */
  public void setApexes(int[] apexes) {
    this.apexes.setValue(Arrays.stream(apexes).boxed().collect(Collectors.toList()));
  }

  /**
   * Find out if the apexes property has been defined.
   *
   * @return True if the apexes property has been defined, false otherwise.
   */
  public boolean isApexesDefined() {
    return this.apexes.isDefined();
  }

  /**
   * Undefine the apexes property.
   */
  public void undefineApexes() {
    this.apexes.undefine();
  }

  /**
   * Get the position property.
   *
   * @return The position property.
   */
  public EntityRefProperty getPositionProperty() {
    return this.position;
  }

  /**
   * Get the position of this blockade.
   *
   * @return The position.
   */
  public EntityID getPosition() {
    return this.position.getValue();
  }

  /**
   * Set the position.
   *
   * @param position The new position.
   */
  public void setPosition(EntityID position) {
    this.position.setValue(position);
  }

  /**
   * Find out if the position property has been defined.
   *
   * @return True if the position property has been defined, false otherwise.
   */
  public boolean isPositionDefined() {
    return this.position.isDefined();
  }

  /**
   * Undefine the position property.
   */
  public void undefinePosition() {
    this.position.undefine();
  }

  /**
   * Get the repair cost property.
   *
   * @return The repair cost property.
   */
  public IntProperty getRepairCostProperty() {
    return this.repairCost;
  }

  /**
   * Get the repair cost of this blockade.
   *
   * @return The repair cost.
   */
  public int getRepairCost() {
    return this.repairCost.getValue();
  }

  /**
   * Set the repair cost.
   *
   * @param cost The new repair cost.
   */
  public void setRepairCost(int cost) {
    this.repairCost.setValue(cost);
  }

  /**
   * Find out if the repair cost property has been defined.
   *
   * @return True if the repair cost property has been defined, false otherwise.
   */
  public boolean isRepairCostDefined() {
    return this.repairCost.isDefined();
  }

  /**
   * Undefine the repair cost property.
   */
  public void undefineRepairCost() {
    this.repairCost.undefine();
  }

  /**
   * Get this area as a Java Shape object.
   *
   * @return A Shape describing this area.
   */
  public Shape getShape() {
    if (this.shape == null) {
      int[] allApexes = getApexes();
      int count = allApexes.length / 2;
      int[] xs = new int[count];
      int[] ys = new int[count];
      for (int i = 0; i < count; ++i) {
        xs[i] = allApexes[i * 2];
        ys[i] = allApexes[i * 2 + 1];
      }
      this.shape = new Polygon(xs, ys, count);
    }
    return this.shape;
  }

  private class ApexesListener implements EntityListener {

    @Override
    public void propertyChanged(Entity e, Property<?> p, Object oldValue, Object newValue) {
      if (p == apexes) {
        shape = null;
      }
    }
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
        case POSITION:
          this.setPosition(this.getPositionProperty().convertToValue(fields));
          break;
        case APEXES:
          List<Integer> apexes = this.getApexesProperty().convertToValue(fields);
          this.setApexes(apexes.stream().mapToInt(i -> i).toArray());
          break;
        case REPAIR_COST:
          this.setRepairCost(this.getRepairCostProperty().convertToValue(fields));
          ;
          break;
        default:
      }
    }
  }
}