package rescuecore2.worldmodel;

/**
   Interface for objects that are interested in hearing about changes to entities.
 */
public interface EntityListener {
    /**
       Notification that a property has changed.
       @param e The entity that has changed.
       @param p The property that has changed.
     */
    void propertyChanged(Entity e, Property p);
}