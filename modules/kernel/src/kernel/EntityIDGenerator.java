package kernel;

import rescuecore2.worldmodel.EntityID;

/**
   Interface for objects that can generate new EntityIDs.
 */
public interface EntityIDGenerator {
    /**
       Create a new EntityID.
       @return A new EntityID.
    */
    EntityID generateID();
}
