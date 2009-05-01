package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.Entity;

/**
   Interface for all legacy entities.
 */
public interface LegacyEntity extends Entity {
    /**
       Get the type of this entity.
       @return The entity type.
     */
    public EntityType getType();
}