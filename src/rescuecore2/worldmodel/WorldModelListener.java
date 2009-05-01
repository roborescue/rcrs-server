package rescuecore2.worldmodel;

/**
   Interface for objects that are interested in changes to the world model.
 */
public interface WorldModelListener {
    /**
       Notification that an Entity was added to the world.
       @param e The entity that was added.
     */
    public void entityAdded(Entity e);

    /**
       Notification that an Entity was removed from the world.
       @param e The entity that was removed.
     */
    public void entityRemoved(Entity e);
}