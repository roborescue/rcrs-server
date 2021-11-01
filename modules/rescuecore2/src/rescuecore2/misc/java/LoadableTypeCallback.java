package rescuecore2.misc.java;

import java.util.Collection;

/**
 * Callback interface for processing loadable types.
 */
public interface LoadableTypeCallback {
  /**
   * Notification that a loadable type was found.
   *
   * @param type      The LoadableType that was found.
   * @param className The class name.
   */
  void classFound(LoadableType type, String className);

  /**
   * Get the set of loadable types that this callback is interested in.
   *
   * @return A collection of LoadableType objects.
   */
  Collection<LoadableType> getTypes();
}