package rescuecore2.worldmodel;

/**
   A factory for vending Entities.
 */
public interface EntityFactory {
    /**
       Create an EntityType object from a numeric ID.
       @param id The ID of the requested entity type.
       @return An EntityType object. This should probably be a shared instance.
     */
    //    T makeEntityType(int id);

    /**
       Create a new Entity with a particular type.
       @param type The type of the entity to create.
       @param id The id of the new entity.
       @return A new Entity of the correct type.
       @throws IllegalArgumentException If the type is not recognised.
     */
    Entity makeEntity(int type, EntityID id);

    /**
       Get all entity type IDs understood by this factory.
       @return All entity type IDs.
    */
    int[] getKnownEntityTypeIDs();
}