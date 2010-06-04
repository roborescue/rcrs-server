package rescuecore2.misc;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
   Handy utility functions.
*/
public final class Handy {
    private Handy() {
    }

    /**
       Turn a collection of Entities into a collection of EntityIDs.
       @param entities The Entities to convert.
       @return A new set of EntityID objects.
    */
    public static Set<EntityID> objectsToIDs(Collection<? extends Entity> entities) {
        Set<EntityID> result = new HashSet<EntityID>();
        for (Entity next : entities) {
            result.add(next.getID());
        }
        return result;
    }
}