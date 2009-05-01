package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityID;

/**
   Civilian class
 */
public class Civilian extends Human {
    public Civilian(EntityID id) {
	super(EntityType.CIVILIAN, id);
    }
}