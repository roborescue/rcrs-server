package rescuecore2.version0.entities;

import rescuecore2.worldmodel.AbstractEntity;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

import java.util.Iterator;

/**
   Abstract base class for all version0 entities.
 */
public abstract class RescueObject extends AbstractEntity {
    /**
       Construct a RescueObject with entirely undefined property values.
       @param id The ID of this entity.
       @param type The type ID of this entity.
       @param props The set of properties this entity has.
     */
    protected RescueObject(EntityID id, EntityType type, Property... props) {
        super(id, type, props);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getType().getName());
        result.append(" [");
        for (Iterator<Property> it = getProperties().iterator(); it.hasNext();) {
            result.append(it.next().toString());
            if (it.hasNext()) {
                result.append(", ");
            }
        }
        result.append("]");
        return result.toString();
    }
}