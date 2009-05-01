package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityID;

/**
   The PoliceOffice object.
 */
public class PoliceOffice extends Building {
    /**
       Construct a PoliceOffice object with entirely undefined values.
     */
    public PoliceOffice(EntityID id) {
	super(EntityType.POLICE_OFFICE, id);
    }
}