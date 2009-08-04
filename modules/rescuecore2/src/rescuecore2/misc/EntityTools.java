package rescuecore2.misc;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.Property;

/**
   A bunch of useful tools for entities.
 */
public final class EntityTools {
    /** Utility class; private constructor. */
    private EntityTools() {}

    /**
       Copy relevant properties from one entity to another.
       @param from The entity to copy property values from.
       @param to The entity to copy property values to.
     */
    public static void copyProperties(Entity from, Entity to) {
        for (Property next : from.getProperties()) {
            Property p = to.getProperty(next.getID());
            if (p != null) {
                p.takeValue(next);
            }
        }
    }
}