package rescuecore2.worldmodel;

import java.util.Collection;

/**
   This class encapsulates everything about a world model. The world model can be parameterised on a subclass of Entity if required.
   @param <T> The subclass of Entity that this world model holds.
*/
public interface WorldModel<T extends Entity> extends Iterable<T> {
    /**
       Add a listener for world model events.
       @param l The listener to add.
    */
    void addWorldModelListener(WorldModelListener<? super T> l);

    /**
       Remove a listener for world model events.
       @param l The listener to remove.
    */
    void removeWorldModelListener(WorldModelListener<? super T> l);

    /**
       Get all entities in the world.
       @return An immutable view of all entities in the world.
    */
    Collection<T> getAllEntities();

    /**
       Add an entity to the world.
       @param e The entity to add.
    */
    void addEntity(T e);

    /**
       Add a set of entities to the world.
       @param e The entities to add.
    */
    void addEntities(Collection<? extends T> e);

    /**
       Remove an entity from the world.
       @param e The entity to remove.
    */
    void removeEntity(T e);

    /**
       Remove an entity from the world.
       @param id The entityID to remove.
    */
    void removeEntity(EntityID id);

    /**
       Remove all entities from the world.
    */
    void removeAllEntities();

    /**
       Look up an entity by ID.
       @param id The EntityID to look up.
       @return The entity with the given ID, or null if no such entity exists.
     */
    T getEntity(EntityID id);

    /**
       Merge a set of entities into this world. New entities will be added, existing entities will have properties replaced with those taken from the given objects.
       @param toMerge The set of entities to merge into this world model.
    */
    void merge(Collection<? extends T> toMerge);
}