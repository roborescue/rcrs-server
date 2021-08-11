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
     */
    protected StandardEntity(EntityID id) {
        super(id);
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

    /**
       Get the URN of this entity type as an instanceof StandardEntityURN.
       @return A StandardEntityURN.
     */
    public abstract StandardEntityURN getStandardURN();

    @Override
    public final String getURN() {
        return getStandardURN().toString();
    }
}
