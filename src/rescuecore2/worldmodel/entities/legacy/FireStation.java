package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityID;

/**
   The FireStation object.
 */
public class FireStation extends Building {
    /**
       Construct a FireStation object with entirely undefined values.
     */
    public FireStation(EntityID id) {
	super(EntityType.FIRE_STATION, id);
    }
}