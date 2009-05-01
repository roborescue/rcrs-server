package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityID;

/**
   AmbulanceTeam class
 */
public class AmbulanceTeam extends Human {
    public AmbulanceTeam(EntityID id) {
	super(EntityType.AMBULANCE_TEAM, id);
    }
}