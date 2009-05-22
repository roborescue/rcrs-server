package rescuecore2.worldmodel;

/**
   A factory for vending Entities.
   @param <T> A subclass of EntityType. This factory will only deal with subclasses of <T>.
   @param <E> A subclass of Entity. This factory will only deal with subclasses of <E>.
 */
public interface EntityFactory<T extends EntityType, E extends Entity> {
    /**
       Create an EntityType object from a numeric ID.
       @param id The ID of the requested entity type.
       @return An EntityType object. This should probably be a shared instance.
     */
    T makeEntityType(int id);

    /**
       Create a new Entity with a particular type.
       @param type The type of the entity to create.
       @param id The id of the new entity.
       @return A new Entity of the correct type.
     */
    E makeEntity(T type, EntityID id);
}