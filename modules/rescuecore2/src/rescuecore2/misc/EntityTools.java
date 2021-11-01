package rescuecore2.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.Property;

/**
 * A bunch of useful tools for entities.
 */
public final class EntityTools {
  /** Utility class; private constructor. */
  private EntityTools() {
  }

  /**
   * Copy relevant properties from one entity to another.
   *
   * @param from The entity to copy property values from.
   * @param to   The entity to copy property values to.
   */
  public static void copyProperties(Entity from, Entity to) {
    for (Property next : from.getProperties()) {
      Property p = to.getProperty(next.getURN());
      if (p != null) {
        p.takeValue(next);
      }
    }
  }

  /**
   * Sort a collection of entities by ID and return a sorted list.
   *
   * @param input The collection to sort.
   * @param <T>   A subtype of Entity.
   * @return A sorted list. If the input is already a list then it will be sorted
   *         in place.
   */
  public static <T extends Entity> List<T> sortedList(Collection<T> input) {
    List<T> result;
    if (input instanceof List) {
      result = (List<T>) input;
    } else {
      result = new ArrayList<T>(input);
    }
    Collections.sort(result, new IDComparator());
    return result;
  }

  /**
   * Comparator that sorts entities by ID.
   */
  public static class IDComparator implements Comparator<Entity>, java.io.Serializable {
    @Override
    public int compare(Entity e1, Entity e2) {
      return e1.getID().getValue() - e2.getID().getValue();
    }
  }
}
