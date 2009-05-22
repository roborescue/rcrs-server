package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.misc.Pair;

import java.util.List;

/**
   Abstract base class for Humans.
 */
public abstract class Human extends RescueEntity {
    private EntityRefProperty position;
    private IntProperty positionExtra;
    private EntityRefListProperty positionHistory;
    private IntProperty direction;
    private IntProperty stamina;
    private IntProperty hp;
    private IntProperty damage;
    private IntProperty buriedness;

    /**
       Construct a subclass of a Human object with entirely undefined property values.
       @param id The ID of this entity.
       @param type The type ID of this entity.
    */
    protected Human(EntityID id, RescueEntityType type) {
        super(id, type);
        position = new EntityRefProperty(RescuePropertyType.POSITION);
        positionExtra = new IntProperty(RescuePropertyType.POSITION_EXTRA);
        positionHistory = new EntityRefListProperty(RescuePropertyType.POSITION_HISTORY);
        direction = new IntProperty(RescuePropertyType.DIRECTION);
        stamina = new IntProperty(RescuePropertyType.STAMINA);
        hp = new IntProperty(RescuePropertyType.HP);
        damage = new IntProperty(RescuePropertyType.DAMAGE);
        buriedness = new IntProperty(RescuePropertyType.BURIEDNESS);
        addProperties(position, positionExtra, positionHistory, direction, stamina, hp, damage, buriedness);
    }

    @Override
    public Pair<Integer, Integer> getLocation(WorldModel<? extends RescueEntity> world) {
        if (!position.isDefined()) {
            return null;
        }
        RescueEntity positionEntity = world.getEntity(position.getValue());
        if (positionEntity == null) {
            return null;
        }
        if (positionEntity instanceof Edge) {
            // Work out the positionExtra
            if (!positionExtra.isDefined()) {
                return positionEntity.getLocation(world);
            }
            int extra = positionExtra.getValue();
            Edge edge = (Edge)positionEntity;
            double d = ((double)extra) / (double)edge.getLength();
            RescueEntity head = edge.getHead(world);
            RescueEntity tail = edge.getTail(world);
            if (head == null || tail == null) {
                return null;
            }
            Pair<Integer, Integer> headLocation = head.getLocation(world);
            Pair<Integer, Integer> tailLocation = tail.getLocation(world);
            double dx = tailLocation.first().intValue() - headLocation.first().intValue();
            double dy = tailLocation.second().intValue() - headLocation.second().intValue();
            dx *= d;
            dy *= d;
            int x = (int)(headLocation.first().intValue() + dx);
            int y = (int)(headLocation.second().intValue() + dy);
            return new Pair<Integer, Integer>(x, y);
        }
        else {
            return positionEntity.getLocation(world);
        }
    }

    /**
       Get the position property.
       @return The position property.
     */
    public EntityRefProperty getPositionProperty() {
        return position;
    }

    /**
       Get the position of this human.
       @return The position.
     */
    public EntityID getPosition() {
        return position.getValue();
    }

    /**
       Set the position of this human.
       @param position The new position.
    */
    public void setPosition(EntityID position) {
        this.position.setValue(position);
    }

    /**
       Find out if the position property has been defined.
       @return True if the position property has been defined, false otherwise.
     */
    public boolean isPositionDefined() {
        return position.isDefined();
    }

    /**
       Undefine the position property.
    */
    public void undefinePosition() {
        position.undefine();
    }
    /**
       Get the position extra property.
       @return The position extra property.
     */
    public IntProperty getPositionExtraProperty() {
        return positionExtra;
    }

    /**
       Get the positionExtra of this human.
       @return The positionExtra of this human.
     */
    public int getPositionExtra() {
        return positionExtra.getValue();
    }

    /**
       Set the positionExtra of this human.
       @param positionExtra The new positionExtra.
    */
    public void setPositionExtra(int positionExtra) {
        this.positionExtra.setValue(positionExtra);
    }

    /**
       Find out if the positionExtra property has been defined.
       @return True if the positionExtra property has been defined, false otherwise.
     */
    public boolean isPositionExtraDefined() {
        return positionExtra.isDefined();
    }

    /**
       Undefine the positionExtra property.
    */
    public void undefinePositionExtra() {
        positionExtra.undefine();
    }

    /**
       Get the position history property.
       @return The position history property.
     */
    public EntityRefListProperty getPositionHistoryProperty() {
        return positionHistory;
    }

    /**
       Get the position history.
       @return The position history.
     */
    public List<EntityID> getPositionHistory() {
        return positionHistory.getValue();
    }

    /**
       Set the position history.
       @param history The new position history.
    */
    public void setPositionHistory(List<EntityID> history) {
        this.positionHistory.setValue(history);
    }

    /**
       Find out if the position history property has been defined.
       @return True if the position history property has been defined, false otherwise.
     */
    public boolean isPositionHistoryDefined() {
        return positionHistory.isDefined();
    }

    /**
       Undefine the position history property.
    */
    public void undefinePositionHistory() {
        positionHistory.undefine();
    }

    /**
       Get the direction property.
       @return The direction property.
     */
    public IntProperty getDirectionProperty() {
        return direction;
    }

    /**
       Get the direction.
       @return The direction.
     */
    public int getDirection() {
        return direction.getValue();
    }

    /**
       Set the direction.
       @param direction The new direction.
    */
    public void setDirection(int direction) {
        this.direction.setValue(direction);
    }

    /**
       Find out if the direction property has been defined.
       @return True if the direction property has been defined, false otherwise.
     */
    public boolean isDirectionDefined() {
        return direction.isDefined();
    }

    /**
       Undefine the direction property.
    */
    public void undefineDirection() {
        direction.undefine();
    }

    /**
       Get the stamina property.
       @return The stamina property.
     */
    public IntProperty getStaminaProperty() {
        return stamina;
    }

    /**
       Get the stamina of this human.
       @return The stamina.
     */
    public int getStamina() {
        return stamina.getValue();
    }

    /**
       Set the stamina of this human.
       @param stamina The new stamina.
    */
    public void setStamina(int stamina) {
        this.stamina.setValue(stamina);
    }

    /**
       Find out if the stamina property has been defined.
       @return True if the stamina property has been defined, false otherwise.
     */
    public boolean isStaminaDefined() {
        return stamina.isDefined();
    }

    /**
       Undefine the stamina property.
    */
    public void undefineStamina() {
        stamina.undefine();
    }

    /**
       Get the hp property.
       @return The hp property.
     */
    public IntProperty getHPProperty() {
        return hp;
    }

    /**
       Get the hp of this human.
       @return The hp of this human.
     */
    public int getHP() {
        return hp.getValue();
    }

    /**
       Set the hp of this human.
       @param newHP The new hp.
    */
    public void setHP(int newHP) {
        this.hp.setValue(newHP);
    }

    /**
       Find out if the hp property has been defined.
       @return True if the hp property has been defined, false otherwise.
     */
    public boolean isHPDefined() {
        return hp.isDefined();
    }

    /**
       Undefine the hp property.
    */
    public void undefineHP() {
        hp.undefine();
    }

    /**
       Get the damage property.
       @return The damage property.
     */
    public IntProperty getDamageProperty() {
        return damage;
    }

    /**
       Get the damage of this human.
       @return The damage of this human.
     */
    public int getDamage() {
        return damage.getValue();
    }

    /**
       Set the damage of this human.
       @param damage The new damage.
    */
    public void setDamage(int damage) {
        this.damage.setValue(damage);
    }

    /**
       Find out if the damage property has been defined.
       @return True if the damage property has been defined, false otherwise.
     */
    public boolean isDamageDefined() {
        return damage.isDefined();
    }

    /**
       Undefine the damage property.
    */
    public void undefineDamage() {
        damage.undefine();
    }

    /**
       Get the buriedness property.
       @return The buriedness property.
     */
    public IntProperty getBuriednessProperty() {
        return buriedness;
    }

    /**
       Get the buriedness of this human.
       @return The buriedness of this human.
     */
    public int getBuriedness() {
        return buriedness.getValue();
    }

    /**
       Set the buriedness of this human.
       @param buriedness The new buriedness.
    */
    public void setBuriedness(int buriedness) {
        this.buriedness.setValue(buriedness);
    }

    /**
       Find out if the buriedness property has been defined.
       @return True if the buriedness property has been defined, false otherwise.
     */
    public boolean isBuriednessDefined() {
        return buriedness.isDefined();
    }

    /**
       Undefine the buriedness property.
    */
    public void undefineBuriedness() {
        buriedness.undefine();
    }
}