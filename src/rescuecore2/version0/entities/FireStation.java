package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;

/**
   The FireStation object.
 */
public class FireStation extends Building {
    /**
       Construct a FireStation object with entirely undefined values.
     */
    public FireStation(EntityID id) {
        super(id, EntityConstants.FIRE_STATION);
    }
}