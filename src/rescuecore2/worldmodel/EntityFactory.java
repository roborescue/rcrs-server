package rescuecore2.worldmodel;

/**
   A factory for vending Entities.
 */
public interface EntityFactory {
    /**
       Create an EntityType object from a numeric ID.
       @param The ID of the requested entity type.
       @return An EntityType object. This should probably be a shared instance.
     */
    EntityType makeEntityType(int id);


    /**
       Create a new Entity with a particular type.
       @param type The type of the entity to create.
       @param id The id of the new entity.
       @return A new Entity of the correct type.
     */
    Entity makeEntity(EntityType type, EntityID id);
}