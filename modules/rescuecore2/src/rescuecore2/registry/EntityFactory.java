package rescuecore2.registry;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
   A factory for vending Entities.
 */
public interface EntityFactory {
    /**
       Create a new Entity.
       @param urn The urn of the entity to create.
       @param id The id of the new entity.
       @return A new Entity of the correct type.
       @throws IllegalArgumentException If the urn is not recognised.
     */
    Entity makeEntity(String urn, EntityID id);

    /**
       Get all entity urns understood by this factory.
       @return All entity urns.
    */
    String[] getKnownEntityURNs();
}
