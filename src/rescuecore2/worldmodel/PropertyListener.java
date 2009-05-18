package rescuecore2.worldmodel;

/**
   Interface for objects that are interested in hearing about changes to properties.
 */
public interface PropertyListener {
    /**
       Notification that a property has changed.
       @param p The property that has changed.
     */
    void propertyChanged(Property p);
}