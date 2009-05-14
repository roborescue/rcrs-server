package rescuecore2.worldmodel;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
   Abstract base class for WorldModel implementations.
   @param <T> The subclass of Entity that this world model holds.
*/
public abstract class AbstractWorldModel<T extends Entity> implements WorldModel<T> {
    private Set<WorldModelListener<? super T>> listeners;

    /**
       Construct a new abstract world model.
    */
    protected AbstractWorldModel() {
        listeners = new HashSet<WorldModelListener<? super T>>();
    }

    @Override
    public final void addWorldModelListener(WorldModelListener<? super T> l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeWorldModelListener(WorldModelListener<? super T> l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    @Override
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

    /**
       Notify listeners that an entity has been added.
       @param e The new entity.
     */
    protected final void fireEntityAdded(T e) {
        for (WorldModelListener<? super T> l : getListeners()) {
            l.entityAdded(e);
        }
    }

    /**
       Notify listeners that an entity has been removed.
       @param e The entity that has been removed.
     */
    protected final void fireEntityRemoved(T e) {
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