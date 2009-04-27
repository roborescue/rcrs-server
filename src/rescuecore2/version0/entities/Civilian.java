package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;

/**
   Civilian class
 */
public class Civilian extends Human {
    public Civilian(EntityID id) {
        super(id, EntityConstants.CIVILIAN);
    }
}