package rescuecore2.misc.java;

/**
   Callback interface for processing loadable types.
 */
public interface LoadableTypeCallback {
    /**
       Notification that a loadable type was found.
       @param type The LoadableType that was found.
       @param className The class name.
    */
    void classFound(LoadableType type, String className);
}