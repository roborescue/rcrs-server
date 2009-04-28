package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;

/**
   The AmbulanceTeam object.
 */
public class AmbulanceTeam extends Human {
    /**
       Construct a AmbulanceTeam object with entirely undefined values.
       @param id The ID of this entity.
     */
    public AmbulanceTeam(EntityID id) {
        super(id, EntityConstants.AMBULANCE_TEAM);
    }
}