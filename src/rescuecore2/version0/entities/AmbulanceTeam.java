package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;

/**
   AmbulanceTeam class
 */
public class AmbulanceTeam extends Human {
    public AmbulanceTeam(EntityID id) {
	super(id, EntityConstants.AMBULANCE_TEAM);
    }
}