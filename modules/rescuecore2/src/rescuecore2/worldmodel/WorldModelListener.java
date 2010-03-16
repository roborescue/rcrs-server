package rescuecore2.worldmodel;

/**
   Interface for objects that are interested in changes to the world model.
   @param <T> The subclass of Entity that this world model listener understands.
 */
public interface WorldModelListener<T extends Entity> {
    /**
       Notification that an Entity was added to the world.
       @param model The WorldModel that was updated.
       @param e The entity that was added.
     */
    void entityAdded(WorldModel<? extends T> model, T e);

    /**
       Notification that an Entity was removed from the world.
       @param model The WorldModel that was updated.
       @param e The entity that was removed.
     */
    void entityRemoved(WorldModel<? extends T> model, T e);
}
