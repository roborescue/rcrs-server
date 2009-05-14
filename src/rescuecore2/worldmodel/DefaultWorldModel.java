package rescuecore2.worldmodel;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
   Default implementation of a WorldModel.
   @param <T> The subclass of Entity that this world model holds.
*/
public class DefaultWorldModel<T extends Entity> extends AbstractWorldModel<T> {
    private Map<EntityID, T> entities;

    /**
       Construct an empty world model.
    */
    public DefaultWorldModel() {
        entities = new HashMap<EntityID, T>();
    }

    @Override
    public final Collection<T> getAllEntities() {
        return Collections.unmodifiableCollection(entities.values());
    }

    @Override
    public final void addEntity(T e) {
        entities.put(e.getID(), e);
        fireEntityAdded(e);
    }

    @Override
    public final void addEntities(Collection<? extends T> e) {
        for (T next : e) {
            addEntity(next);
        }
    }

    @Override
    public final void removeEntity(T e) {
        if (entities.remove(e.getID()) != null) {
            fireEntityRemoved(e);
        }
    }

    @Override
    public final void removeEntity(EntityID id) {
        T removed = entities.remove(id);
        if (removed != null) {
            fireEntityRemoved(removed);
        }
    }

    @Override
    public final void removeAllEntities() {
        Set<T> all = new HashSet<T>(entities.values());
        entities.clear();
        for (T next : all) {
            fireEntityRemoved(next);
        }
    }

    @Override
    public final T getEntity(EntityID id) {
        return entities.get(id);
    }

    @Override
    public final Iterator<T> iterator() {
        return entities.values().iterator();
    }
}