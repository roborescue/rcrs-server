package rescuecore2.worldmodel;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
   This class encapsulates everything about a world model. The world model can be parameterised on a subclass of Entity if required.
   @param <T> The subclass of Entity that this world model holds.
*/
public class WorldModel<T extends Entity> implements Iterable<T> {
    private Map<EntityID, T> entities;
    private Set<WorldModelListener<? super T>> listeners;

    /**
       Construct an empty world model.
    */
    public WorldModel() {
        entities = new HashMap<EntityID, T>();
        listeners = new HashSet<WorldModelListener<? super T>>();
    }

    /**
       Add a listener for world model events.
       @param l The listener to add.
    */
    public void addWorldModelListener(WorldModelListener<? super T> l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    /**
       Remove a listener for world model events.
       @param l The listener to remove.
    */
    public void removeWorldModelListener(WorldModelListener<? super T> l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
       Get all entities in the world.
       @return An immutable view of all entities in the world.
    */
    public Collection<T> getAllEntities() {
        return Collections.unmodifiableCollection(entities.values());
    }

    /**
       Add an entity to the world.
       @param e The entity to add.
    */
    public void addEntity(T e) {
        entities.put(e.getID(), e);
        fireEntityAdded(e);
    }

    /**
       Add a set of entities to the world.
       @param e The entities to add.
    */
    public void addEntities(Collection<? extends T> e) {
        for (T next : e) {
            addEntity(next);
        }
    }

    /**
       Remove an entity from the world.
       @param e The entity to remove.
    */
    public void removeEntity(T e) {
        if (entities.remove(e.getID()) != null) {
            fireEntityRemoved(e);
        }
    }

    /**
       Remove an entity from the world.
       @param id The entityID to remove.
    */
    public void removeEntity(EntityID id) {
        T removed = entities.remove(id);
        if (removed != null) {
            fireEntityRemoved(removed);
        }
    }

    /**
       Remove all entities from the world.
    */
    public void removeAllEntities() {
        Set<T> all = new HashSet<T>(entities.values());
        entities.clear();
        for (T next : all) {
            fireEntityRemoved(next);
        }
    }

    /**
       Look up an entity by ID.
       @param id The EntityID to look up.
       @return The entity with the given ID, or null if no such entity exists.
     */
    public T getEntity(EntityID id) {
        return entities.get(id);
    }

    /**
       Merge a set of entities into this world. New entities will be added, existing entities will have properties replaced with those taken from the given objects.
       @param toMerge The set of entities to merge into this world model.
    */
    public void merge(Collection<? extends T> toMerge) {
        for (T next : toMerge) {
            Entity existing = getEntity(next.getID());
            if (existing == null) {
                addEntity(next);
            }
            else {
                for (Property prop : existing.getProperties()) {
                    Property other = next.getProperty(prop.getID());
                    if (other.isDefined()) {
                        prop.takeValue(other);
                    }
                }
            }
        }
    }

    @Override
    public Iterator<T> iterator() {
        return entities.values().iterator();
    }

    private void fireEntityAdded(T e) {
        for (WorldModelListener<? super T> l : getListeners()) {
            l.entityAdded(e);
        }
    }

    private void fireEntityRemoved(T e) {
        for (WorldModelListener<? super T> l : getListeners()) {
            l.entityRemoved(e);
        }
    }

    private Collection<WorldModelListener<? super T>> getListeners() {
        synchronized (listeners) {
            return new HashSet<WorldModelListener<? super T>>(listeners);
        }
    }
}