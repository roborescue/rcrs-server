package rescuecore2.standard.entities;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

  private IntProperty x;
  private IntProperty y;
  private EntityRefProperty position;
  private IntArrayProperty positionHistory;
  private IntProperty travelDistance;
  private IntProperty direction;
  private IntProperty stamina;
  private IntProperty hp;
  private IntProperty damage;
  private IntProperty buriedness;

  /**
   * Construct a Human object with entirely undefined property values.
   *
   * @param id The ID of this entity.
   */
  protected Human(EntityID id) {
    super(id);
    this.x = new IntProperty(StandardPropertyURN.X);
    this.y = new IntProperty(StandardPropertyURN.Y);
    this.travelDistance = new IntProperty(StandardPropertyURN.TRAVEL_DISTANCE);
    this.position = new EntityRefProperty(StandardPropertyURN.POSITION);
    this.positionHistory = new IntArrayProperty(StandardPropertyURN.POSITION_HISTORY);
    this.direction = new IntProperty(StandardPropertyURN.DIRECTION);
    this.stamina = new IntProperty(StandardPropertyURN.STAMINA);
    this.hp = new IntProperty(StandardPropertyURN.HP);
    this.damage = new IntProperty(StandardPropertyURN.DAMAGE);
    this.buriedness = new IntProperty(StandardPropertyURN.BURIEDNESS);
    registerProperties(this.x, this.y, this.position, this.positionHistory, this.travelDistance, this.direction,
        this.stamina, this.hp, this.damage, this.buriedness);
  }

  /**
   * Human copy constructor.
   *
   * @param other The Human to copy.
   */
  public Human(Human other) {
    super(other);
    this.x = new IntProperty(other.x);
    this.y = new IntProperty(other.y);
    this.travelDistance = new IntProperty(other.travelDistance);
    this.position = new EntityRefProperty(other.position);
    this.positionHistory = new IntArrayProperty(other.positionHistory);
    this.direction = new IntProperty(other.direction);
    this.stamina = new IntProperty(other.stamina);
    this.hp = new IntProperty(other.hp);
    this.damage = new IntProperty(other.damage);
    this.buriedness = new IntProperty(other.buriedness);
    registerProperties(this.x, this.y, this.position, this.positionHistory, this.travelDistance, this.direction,
        this.stamina, this.hp, this.damage, this.buriedness);
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
      case POSITION:
        return this.position;
      case POSITION_HISTORY:
        return this.positionHistory;
      case DIRECTION:
        return this.direction;
      case STAMINA:
        return this.stamina;
      case HP:
        return this.hp;
      case X:
        return this.x;
      case Y:
        return this.y;
      case DAMAGE:
        return this.damage;
      case BURIEDNESS:
        return this.buriedness;
      case TRAVEL_DISTANCE:
        return this.travelDistance;
      default:
        return super.getProperty(urn);
    }
  }

  @Override
  public Pair<Integer, Integer> getLocation(WorldModel<? extends StandardEntity> world) {
    if (this.x.isDefined() && this.y.isDefined()) {
      return new Pair<Integer, Integer>(this.x.getValue(), this.y.getValue());
    }
    if (this.position.isDefined()) {
      EntityID pos = getPosition();
      StandardEntity e = world.getEntity(pos);
      return e.getLocation(world);
    }
    return null;
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
   * Get the position of this human.
   *
   * @return The position.
   */
  public EntityID getPosition() {
    return this.position.getValue();
  }

  /**
   * Set the position of this human.
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
   * Get the position history property.
   *
   * @return The position history property.
   */
  public IntArrayProperty getPositionHistoryProperty() {
    return this.positionHistory;
  }

  /**
   * Get the position history.
   *
   * @return The position history.
   */
  public int[] getPositionHistory() {
    return this.positionHistory.getValue().stream().mapToInt(i -> i).toArray();
  }

  /**
   * Set the position history.
   *
   * @param history The new position history.
   */
  public void setPositionHistory(int[] history) {
    this.positionHistory.setValue(Arrays.stream(history).boxed().collect(Collectors.toList()));
  }

  /**
   * Find out if the position history property has been defined.
   *
   * @return True if the position history property has been defined, false
   *         otherwise.
   */
  public boolean isPositionHistoryDefined() {
    return this.positionHistory.isDefined();
  }

  /**
   * Undefine the position history property.
   */
  public void undefinePositionHistory() {
    this.positionHistory.undefine();
  }

  /**
   * Get the direction property.
   *
   * @return The direction property.
   */
  public IntProperty getDirectionProperty() {
    return this.direction;
  }

  /**
   * Get the direction.
   *
   * @return The direction.
   */
  public int getDirection() {
    return this.direction.getValue();
  }

  /**
   * Set the direction.
   *
   * @param direction The new direction.
   */
  public void setDirection(int direction) {
    this.direction.setValue(direction);
  }

  /**
   * Find out if the direction property has been defined.
   *
   * @return True if the direction property has been defined, false otherwise.
   */
  public boolean isDirectionDefined() {
    return this.direction.isDefined();
  }

  /**
   * Undefine the direction property.
   */
  public void undefineDirection() {
    this.direction.undefine();
  }

  /**
   * Get the stamina property.
   *
   * @return The stamina property.
   */
  public IntProperty getStaminaProperty() {
    return this.stamina;
  }

  /**
   * Get the stamina of this human.
   *
   * @return The stamina.
   */
  public int getStamina() {
    return this.stamina.getValue();
  }

  /**
   * Set the stamina of this human.
   *
   * @param stamina The new stamina.
   */
  public void setStamina(int stamina) {
    this.stamina.setValue(stamina);
  }

  /**
   * Find out if the stamina property has been defined.
   *
   * @return True if the stamina property has been defined, false otherwise.
   */
  public boolean isStaminaDefined() {
    return this.stamina.isDefined();
  }

  /**
   * Undefine the stamina property.
   */
  public void undefineStamina() {
    this.stamina.undefine();
  }

  /**
   * Get the hp property.
   *
   * @return The hp property.
   */
  public IntProperty getHPProperty() {
    return this.hp;
  }

  /**
   * Get the hp of this human.
   *
   * @return The hp of this human.
   */
  public int getHP() {
    return this.hp.getValue();
  }

  /**
   * Set the hp of this human.
   *
   * @param newHP The new hp.
   */
  public void setHP(int newHP) {
    this.hp.setValue(newHP);
  }

  /**
   * Find out if the hp property has been defined.
   *
   * @return True if the hp property has been defined, false otherwise.
   */
  public boolean isHPDefined() {
    return this.hp.isDefined();
  }

  /**
   * Undefine the hp property.
   */
  public void undefineHP() {
    this.hp.undefine();
  }

  /**
   * Get the damage property.
   *
   * @return The damage property.
   */
  public IntProperty getDamageProperty() {
    return this.damage;
  }

  /**
   * Get the damage of this human.
   *
   * @return The damage of this human.
   */
  public int getDamage() {
    return this.damage.getValue();
  }

  /**
   * Set the damage of this human.
   *
   * @param damage The new damage.
   */
  public void setDamage(int damage) {
    this.damage.setValue(damage);
  }

  /**
   * Find out if the damage property has been defined.
   *
   * @return True if the damage property has been defined, false otherwise.
   */
  public boolean isDamageDefined() {
    return this.damage.isDefined();
  }

  /**
   * Undefine the damage property.
   */
  public void undefineDamage() {
    this.damage.undefine();
  }

  /**
   * Get the buriedness property.
   *
   * @return The buriedness property.
   */
  public IntProperty getBuriednessProperty() {
    return this.buriedness;
  }

  /**
   * Get the buriedness of this human.
   *
   * @return The buriedness of this human.
   */
  public int getBuriedness() {
    return this.buriedness.getValue();
  }

  /**
   * Set the buriedness of this human.
   *
   * @param buriedness The new buriedness.
   */
  public void setBuriedness(int buriedness) {
    this.buriedness.setValue(buriedness);
  }

  /**
   * Find out if the buriedness property has been defined.
   *
   * @return True if the buriedness property has been defined, false otherwise.
   */
  public boolean isBuriednessDefined() {
    return this.buriedness.isDefined();
  }

  /**
   * Undefine the buriedness property.
   */
  public void undefineBuriedness() {
    this.buriedness.undefine();
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
   * Get the X coordinate of this human.
   *
   * @return The x coordinate of this human.
   */
  public int getX() {
    return this.x.getValue();
  }

  /**
   * Set the X coordinate of this human.
   *
   * @param x The new x coordinate.
   */
  public void setX(int x) {
    this.x.setValue(x);
  }

  /**
   * Find out if the x property has been defined.
   *
   * @return True if the x property has been defined, false otherwise.
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
   * Get the y property.
   *
   * @return The y property.
   */
  public IntProperty getYProperty() {
    return this.y;
  }

  /**
   * Get the y coordinate of this human.
   *
   * @return The y coordinate of this human.
   */
  public int getY() {
    return this.y.getValue();
  }

  /**
   * Set the y coordinate of this human.
   *
   * @param y The new y coordinate.
   */
  public void setY(int y) {
    this.y.setValue(y);
  }

  /**
   * Find out if the y property has been defined.
   *
   * @return True if the y property has been defined, false otherwise.
   */
  public boolean isYDefined() {
    return this.y.isDefined();
  }

  /**
   * Undefine the y property.
   */
  public void undefineY() {
    this.y.undefine();
  }

  /**
   * Get the travel distance property.
   *
   * @return The travel distance property.
   */
  public IntProperty getTravelDistanceProperty() {
    return this.travelDistance;
  }

  /**
   * Get the travel distance.
   *
   * @return The travel distance.
   */
  public int getTravelDistance() {
    return this.travelDistance.getValue();
  }

  /**
   * Set the travel distance.
   *
   * @param d The new travel distance.
   */
  public void setTravelDistance(int d) {
    this.travelDistance.setValue(d);
  }

  /**
   * Find out if the travel distance property has been defined.
   *
   * @return True if the travel distance property has been defined, false
   *         otherwise.
   */
  public boolean isTravelDistanceDefined() {
    return this.travelDistance.isDefined();
  }

  /**
   * Undefine the travel distance property.
   */
  public void undefineTravelDistance() {
    this.travelDistance.undefine();
  }

  /**
   * Get the entity represented by the position property. The result will be null
   * if the position property has not been set or if the entity reference is
   * invalid.
   *
   * @param model The WorldModel to look up entity references.
   * @return The entity represented by the position property.
   */
  public StandardEntity getPosition(WorldModel<? extends StandardEntity> model) {
    if (!this.position.isDefined()) {
      return null;
    }
    return model.getEntity(this.position.getValue());
  }

  /**
   * Set the position of this human.
   *
   * @param newPosition The new position.
   * @param newX        The x coordinate of this agent.
   * @param newY        The y coordinate if this agent.
   */
  public void setPosition(EntityID newPosition, int newX, int newY) {
    this.position.setValue(newPosition);
    this.x.setValue(newX);
    this.y.setValue(newY);
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
        case POSITION_HISTORY:
          List<Integer> history = this.getPositionHistoryProperty().convertToValue(fields);
          this.setPositionHistory(history.stream().mapToInt(i -> i).toArray());
          break;
        case DIRECTION:
          this.setDirection(this.getDirectionProperty().convertToValue(fields));
          break;
        case STAMINA:
          this.setStamina(this.getStaminaProperty().convertToValue(fields));
          break;
        case HP:
          this.setHP(this.getHPProperty().convertToValue(fields));
          break;
        case DAMAGE:
          this.setDamage(this.getDamageProperty().convertToValue(fields));
          break;
        case BURIEDNESS:
          this.setBuriedness(this.getBuriednessProperty().convertToValue(fields));
          break;
        case TRAVEL_DISTANCE:
          this.setTravelDistance(this.getTravelDistanceProperty().convertToValue(fields));
          break;
        default:
      }
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject jsonObject = super.toJson();
    jsonObject.put(StandardPropertyURN.DAMAGE.toString(), this.isDamageDefined() ? this.getDamage() : JSONObject.NULL);
    jsonObject.put(StandardPropertyURN.HP.toString(), this.isHPDefined() ? this.getHP() : JSONObject.NULL);
    jsonObject.put(StandardPropertyURN.POSITION.toString(),
        this.isPositionDefined() ? new int[] { getX(), getY() } : JSONObject.NULL);
    jsonObject.put(StandardPropertyURN.POSITION_HISTORY.toString(),
        this.isPositionHistoryDefined() ? this.getPositionHistory() : JSONObject.NULL);

    return jsonObject;
  }
}