package rescuecore2.standard.entities;

import rescuecore2.worldmodel.AbstractEntity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.misc.Pair;

/**
   Abstract base class for all standard entities.
 */
public abstract class StandardEntity extends AbstractEntity {
    /**
       Construct a StandardEntity with entirely undefined property values.
       @param id The ID of this entity.
       @param urn The urn of this entity type.
     */
    protected StandardEntity(EntityID id, StandardEntityURN urn) {
        super(id, urn);
    }

    /**
       StandardEntity copy constructor.
       @param other The StandardEntity to copy.
     */
    protected StandardEntity(StandardEntity other) {
        super(other);
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