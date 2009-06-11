package rescuecore2.standard.entities;

import rescuecore2.worldmodel.AbstractEntity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.misc.Pair;

/**
   Abstract base class for all standard entities.
 */
public abstract class StandardEntity extends AbstractEntity<StandardEntityType> {
    /**
       Construct a StandardEntity with entirely undefined property values.
       @param id The ID of this entity.
       @param type The type ID of this entity.
     */
    protected StandardEntity(EntityID id, StandardEntityType type) {
        super(id, type);
    }

    /**
       Get the location of this entity.
       @param world The world model to look up for entity references.
       @return The coordinates of this entity, or null if the location cannot be determined.
     */
    public Pair<Integer, Integer> getLocation(WorldModel<? extends StandardEntity> world) {
        return null;
    }
}