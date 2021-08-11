package rescuecore2.worldmodel;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import rescuecore2.registry.Registry;

/**
   Abstract base class for WorldModel implementations.
   @param <T> The subclass of Entity that this world model holds.
*/
public abstract class AbstractWorldModel<T extends Entity> implements WorldModel<T> {
    private Set<WorldModelListener<? super T>> listeners;
    private Collection<Class<? extends T>> allowedClasses;

    /**
       Construct a new abstract world model.
    */
    protected AbstractWorldModel() {
        listeners = new HashSet<WorldModelListener<? super T>>();
        allowedClasses = new HashSet<Class<? extends T>>();
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
    @SuppressWarnings("unchecked")
    public final void addEntity(Entity e) {
        if (isAllowed(e)) {
            addEntityImpl((T)e);
        }
        else {
            throw new IllegalArgumentException(this + " does not accept entities of type " + e.getClass().getName());
        }
    }

    @Override
    public final void addEntities(Collection<? extends Entity> e) {
        for (Entity next : e) {
            addEntity(next);
        }
    }

    @Override
    public final void removeEntity(T e) {
        removeEntity(e.getID());
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
                    Property other = next.getProperty(prop.getURN());
                    if (other.isDefined()) {
                        prop.takeValue(other);
                    }
                }
            }
        }
    }

    @Override
    public void merge(ChangeSet changeSet) {
        for (EntityID e : changeSet.getChangedEntities()) {
            Entity existingEntity = getEntity(e);
            boolean add = false;
            if (existingEntity == null) {
                // Construct a new entity
                existingEntity = Registry.getCurrentRegistry().createEntity(changeSet.getEntityURN(e), e);
                if (existingEntity == null) {
                    // Bail out
                    continue;
                }
                add = true;
            }
            for (Property p : changeSet.getChangedProperties(e)) {
                Property existingProperty = existingEntity.getProperty(p.getURN());
                existingProperty.takeValue(p);
            }
            if (add) {
                addEntity(existingEntity);
            }
        }
        for (EntityID next : changeSet.getDeletedEntities()) {
            removeEntity(next);
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
       Find out if a particular Entity is allowed into this world model. The default implementation checks that the object is assignable to at least one class previously registered with {@link #registerAllowedClass(Class)}.
       @param e The entity to check.
       @return True if the entity is allowed, false otherwise.
     */
    protected boolean isAllowed(Entity e) {
        for (Class<? extends T> next : allowedClasses) {
            if (next.isAssignableFrom(e.getClass())) {
                return true;
            }
        }
        return false;
    }

    /**
       Register an allowed class.
       @param clazz The subclass of Entity that is allowed in this world model.
    */
    protected void registerAllowedClass(Class<? extends T> clazz) {
        allowedClasses.add(clazz);
    }

    private Collection<WorldModelListener<? super T>> getListeners() {
        synchronized (listeners) {
            return new HashSet<WorldModelListener<? super T>>(listeners);
        }
    }
}
