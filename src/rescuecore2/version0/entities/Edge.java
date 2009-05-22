package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.misc.Pair;


/**
   Edge-type entities (e.g. roads).
 */
public abstract class Edge extends RescueEntity {
    private EntityRefProperty head;
    private EntityRefProperty tail;
    private IntProperty length;

    /**
       Construct an Edge object with entirely undefined property values.
       @param id The ID of this entity.
       @param type The type ID of this entity.
     */
    protected Edge(EntityID id, RescueEntityType type) {
        super(id, type);
        head = new EntityRefProperty(RescuePropertyType.HEAD);
        tail = new EntityRefProperty(RescuePropertyType.TAIL);
        length = new IntProperty(RescuePropertyType.LENGTH);
        addProperties(head, tail, length);
    }

    @Override
    public Pair<Integer, Integer> getLocation(WorldModel<? extends RescueEntity> world) {
        RescueEntity headEntity = world.getEntity(head.getValue());
        RescueEntity tailEntity = world.getEntity(tail.getValue());
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
       Get the length property.
       @return The length property.
     */
    public IntProperty getLengthProperty() {
        return length;
    }

    /**
       Get the length of this edge.
       @return The length of this edge.
     */
    public int getLength() {
        return length.getValue();
    }

    /**
       Set the length of this edge.
       @param length The new length.
    */
    public void setLength(int length) {
        this.length.setValue(length);
    }

    /**
       Find out if the length property has been defined.
       @return True if the length property has been defined, false otherwise.
     */
    public boolean isLengthDefined() {
        return length.isDefined();
    }

    /**
       Undefine the length property.
    */
    public void undefineLength() {
        length.undefine();
    }

    /**
       Get the head property.
       @return The head property.
     */
    public EntityRefProperty getHeadProperty() {
        return head;
    }

    /**
       Get the head.
       @return The head.
     */
    public EntityID getHead() {
        return head.getValue();
    }

    /**
       Set the head of this edge.
       @param head The new head.
    */
    public void setHead(EntityID head) {
        this.head.setValue(head);
    }

    /**
       Find out if the head property has been defined.
       @return True if the head property has been defined, false otherwise.
     */
    public boolean isHeadDefined() {
        return head.isDefined();
    }

    /**
       Undefine the head property.
    */
    public void undefineHead() {
        head.undefine();
    }

    /**
       Get the tail property.
       @return The tail property.
     */
    public EntityRefProperty getTailProperty() {
        return tail;
    }

    /**
       Get the tail of this edge.
       @return The tail of this edge.
     */
    public EntityID getTail() {
        return tail.getValue();
    }

    /**
       Set the tail of this edge.
       @param tail The new tail.
    */
    public void setTail(EntityID tail) {
        this.tail.setValue(tail);
    }

    /**
       Find out if the tail property has been defined.
       @return True if the tail property has been defined, false otherwise.
     */
    public boolean isTailDefined() {
        return tail.isDefined();
    }

    /**
       Undefine the tail property.
    */
    public void undefineTail() {
        tail.undefine();
    }

    /**
       Get the entity represented by the HEAD property. The result will be null if the HEAD property has not been set or if the entity reference is invalid.
       @param model The WorldModel to look up entity references.
       @return The entity represented by the HEAD property.
     */
    public RescueEntity getHead(WorldModel<? extends RescueEntity> model) {
        if (!head.isDefined()) {
            return null;
        }
        return model.getEntity(head.getValue());
    }

    /**
       Get the entity represented by the TAIL property. The result will be null if the TAIL property has not been set or if the entity reference is invalid.
       @param model The WorldModel to look up entity references.
       @return The entity represented by the TAIL property.
     */
    public RescueEntity getTail(WorldModel<? extends RescueEntity> model) {
        if (!tail.isDefined()) {
            return null;
        }
        return model.getEntity(tail.getValue());
    }
}