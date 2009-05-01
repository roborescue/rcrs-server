package rescuecore2.worldmodel;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
   This class encapsulates everything about a world model.
 */
public class WorldModel {
    private Set<Entity> entities;
    private Set<WorldModelListener> listeners;

    public WorldModel() {
	entities = new HashSet<Entity>();
	listeners = new HashSet<WorldModelListener>();
    }

    /**
       Add a listener for world model events.
       @param l The listener to add.
     */
    public void addWorldModelListener(WorldModelListener l) {
	synchronized(listeners) {
	    listeners.add(l);
	}
    }

    /**
       Remove a listener for world model events.
       @param l The listener to remove.
     */
    public void removeWorldModelListener(WorldModelListener l) {
	synchronized(listeners) {
	    listeners.remove(l);
	}
    }

    /**
       Get all entities in the world.
       @return An immutable view of all entities in the world.
     */
    public Set<Entity> getAllEntities() {
	return Collections.unmodifiableSet(entities);
    }

    /**
       Add an entity to the world.
       @param e The entity to add.
     */
    public void addEntity(Entity e) {
	entities.add(e);
	fireEntityAdded(e);
    }

    /**
       Add a set of entities to the world.
       @param e The entities to add.
     */
    public void addEntities(Set<Entity> e) {
	entities.addAll(e);
	for (Entity next : e) {
	    fireEntityAdded(next);
	}
    }

    /**
       Remove an entity from the world.
       @param e The entity to remove.
     */
    public void removeEntity(Entity e) {
	entities.remove(e);
	fireEntityRemoved(e);
    }

    /**
       Remove all entities from the world.
     */
    public void removeAllEntities() {
	Set<Entity> copy = new HashSet<Entity>(entities);
	entities.clear();
	for (Entity e : copy) {
	    fireEntityRemoved(e);
	}
    }

    private void fireEntityAdded(Entity e) {
	for (WorldModelListener l : getListeners()) {
	    l.entityAdded(e);
	}
    }

    private void fireEntityRemoved(Entity e) {
	for (WorldModelListener l : getListeners()) {
	    l.entityRemoved(e);
	}
    }

    private WorldModelListener[] getListeners() {
	WorldModelListener[] l;
	synchronized(listeners) {
	    l = new WorldModelListener[listeners.size()];
	    listeners.toArray(l);
	}
	return l;
    }
}