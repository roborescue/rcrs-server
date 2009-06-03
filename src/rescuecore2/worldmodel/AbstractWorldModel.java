package rescuecore2.worldmodel;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

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
    public final void addEntity(Entity e) {
        System.out.println("Checking if we can add " + e);
        if (isAllowed(e)) {
            System.out.println("Allowed");
            addEntityImpl((T)e);
        }
        else {
            System.out.println("Not allowed");
        }
    }

    @Override
    public final void addEntities(Collection<? extends Entity> e) {
        for (Entity next : e) {
            addEntity(next);
        }
    }

    /**
       Subclasses should provide their implementation of addEntity here.
       @param t The entity to add.
    */
    protected abstract void addEntityImpl(T t);

    @Override
    public void merge(Collection<? extends Entity> toMerge) {
        for (Entity next : toMerge) {
            T existing = getEntity(next.getID());
            if (existing == null) {
                addEntity(next);
            }
            else {
                Set<Property> props = existing.getProperties();
                for (Property prop : props) {
                    Property other = next.getProperty(prop.getType());
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
            l.entityAdded(this, e);
        }
    }

    /**
       Notify listeners that an entity has been removed.
       @param e The entity that has been removed.
     */
    protected final void fireEntityRemoved(T e) {
        for (WorldModelListener<? super T> l : getListeners()) {
            l.entityRemoved(this, e);
        }
    }

    /**
       Find out if a particular Entity is allowed into this world model. The default implementation checks that the object is assignable to at least one class returned by {@link #getAllowedClasses}.
       @param e The entity to check.
       @return True if the entity is allowed, false otherwise.
     */
    protected boolean isAllowed(Entity e) {
        for (Class<? extends T> next : getAllowedClasses()) {
            if (next.isAssignableFrom(e.getClass())) {
                return true;
            }
        }
        return false;
    }

    private Collection<WorldModelListener<? super T>> getListeners() {
        synchronized (listeners) {
            return new HashSet<WorldModelListener<? super T>>(listeners);
        }
    }
}