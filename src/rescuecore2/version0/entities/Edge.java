package rescuecore2.version0.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.misc.Pair;

import rescuecore2.version0.entities.properties.EntityRefProperty;
import rescuecore2.version0.entities.properties.IntProperty;
import rescuecore2.version0.entities.properties.PropertyType;

/**
   Edge-type entities (e.g. roads).
 */
public abstract class Edge extends RescueObject {
    private EntityRefProperty head;
    private EntityRefProperty tail;
    private IntProperty length;

    /**
       Construct an Edge object with entirely undefined property values.
       @param id The ID of this entity.
       @param type The type ID of this entity.
       @param props The set of properties this entity has.
     */
    protected Edge(EntityID id, EntityType type, Property... props) {
        super(id, type, props);
        head = new EntityRefProperty(PropertyType.HEAD);
        tail = new EntityRefProperty(PropertyType.TAIL);
        length = new IntProperty(PropertyType.LENGTH);
        addProperties(head, tail, length);
    }

    @Override
    public Pair<Integer, Integer> getLocation(WorldModel<? extends RescueObject> world) {
        RescueObject headEntity = world.getEntity(head.getValue());
        RescueObject tailEntity = world.getEntity(tail.getValue());
        if (headEntity == null || tailEntity == null) {
            return null;
        }
        Pair<Integer, Integer> headLocation = headEntity.getLocation(world);
        Pair<Integer, Integer> tailLocation = tailEntity.getLocation(world);
        if (headLocation == null || tailLocation == null) {
            return null;
        }
        int x = (headLocation.first().intValue() + tailLocation.first().intValue()) / 2;
        int y = (headLocation.second().intValue() + tailLocation.second().intValue()) / 2;
        return new Pair<Integer, Integer>(x, y);
    }

    /**
       Get the length of this edge. The result is undefined if the LENGTH property has not been set.
       @return The value of the LENGTH property.
     */
    public int getLength() {
        return length.getValue();
    }

    /**
       Find out if the length of this edge has been defined.
       @return True if the length property has been defined, false otherwise.
     */
    public boolean isLengthDefined() {
        return length.isDefined();
    }

    /**
       Get the value of the HEAD property. The result is undefined if the HEAD property has not been set.
       @return The value of the HEAD property.
     */
    public EntityID getHead() {
        return head.getValue();
    }

    /**
       Get the entity represented by the HEAD property. The result will be null if the HEAD property has not been set or if the entity reference is invalid.
       @param world The WorldModel to look up entity references.
       @return The entity represented by the HEAD property.
     */
    public RescueObject getHead(WorldModel<? extends RescueObject> model) {
        if (!head.isDefined()) {
            return null;
        }
        return model.getEntity(head.getValue());
    }

    /**
       Find out if the head of this edge has been defined.
       @return True if the HEAD property has been defined, false otherwise.
     */
    public boolean isHeadDefined() {
        return head.isDefined();
    }

    /**
       Get the value of the TAIL property. The result is undefined if the TAIL property has not been set.
       @return The value of the TAIL property.
     */
    public EntityID getTail() {
        return tail.getValue();
    }

    /**
       Find out if the tail of this edge has been defined.
       @return True if the TAIL property has been defined, false otherwise.
     */
    public boolean isTailDefined() {
        return tail.isDefined();
    }

    /**
       Get the entity represented by the TAIL property. The result will be null if the TAIL property has not been set or if the entity reference is invalid.
       @param world The WorldModel to look up entity references.
       @return The entity represented by the TAIL property.
     */
    public RescueObject getTail(WorldModel<? extends RescueObject> model) {
        if (!tail.isDefined()) {
            return null;
        }
        return model.getEntity(tail.getValue());
    }
}