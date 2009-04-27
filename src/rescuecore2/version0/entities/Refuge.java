package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;

/**
   The Refuge object.
 */
public class Refuge extends Building {
    /**
       Construct a Refuge object with entirely undefined values.
     */
    public Refuge(EntityID id) {
	super(id, EntityConstants.REFUGE);
    }
}