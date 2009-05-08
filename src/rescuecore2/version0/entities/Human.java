package rescuecore2.version0.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.version0.entities.properties.IntProperty;
import rescuecore2.version0.entities.properties.IntArrayProperty;
import rescuecore2.version0.entities.properties.EntityRefProperty;
import rescuecore2.version0.entities.properties.PropertyType;
import rescuecore2.misc.Pair;

/**
   Abstract base class for Humans.
 */
public abstract class Human extends RescueObject {
    private EntityRefProperty position;
    private IntProperty positionExtra;
    private IntArrayProperty positionHistory;
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
    protected Human(EntityID id, EntityType type) {
        super(id, type);
        position = new EntityRefProperty(PropertyType.POSITION);
        positionExtra = new IntProperty(PropertyType.POSITION_EXTRA);
        positionHistory = new IntArrayProperty(PropertyType.POSITION_HISTORY);
        direction = new IntProperty(PropertyType.DIRECTION);
        stamina = new IntProperty(PropertyType.STAMINA);
        hp = new IntProperty(PropertyType.HP);
        damage = new IntProperty(PropertyType.DAMAGE);
        buriedness = new IntProperty(PropertyType.BURIEDNESS);
        addProperties(position, positionExtra, positionHistory, direction, stamina, hp, damage, buriedness);
    }

    public Pair<Integer, Integer> getLocation(WorldModel<? extends RescueObject> world) {
        if (!position.isDefined()) {
            return null;
        }
        RescueObject positionEntity = world.getEntity(position.getValue());
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
            RescueObject head = edge.getHead(world);
            RescueObject tail = edge.getTail(world);
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
}