package rescuecore2.worldmodel;

/**
   Interface for objects that are interested in hearing about changes to entities.
 */
public interface EntityListener {
    /**
       Notification that a property has changed.
       @param e The entity that has changed.
       @param p The property that has changed.
       @param oldValue The old value of the property. This may be null if the property was undefined.
       @param newValue The new value of the property. This may be null if the property is now undefined.
     */
    void propertyChanged(Entity e, Property p, Object oldValue, Object newValue);
}
