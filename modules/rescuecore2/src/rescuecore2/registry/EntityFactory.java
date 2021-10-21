package rescuecore2.registry;


import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
   A factory for vending Entities.
 */
public interface EntityFactory extends Factory {
    /**
       Create a new Entity.
       @param urn The urn of the entity to create.
       @param id The id of the new entity.
       @return A new Entity of the correct type.
       @throws IllegalArgumentException If the urn is not recognised.
     */
    Entity makeEntity(int urn, EntityID id);

}
