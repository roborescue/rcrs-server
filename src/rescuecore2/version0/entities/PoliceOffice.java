package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;

/**
   The PoliceOffice object.
 */
public class PoliceOffice extends Building {
    /**
       Construct a PoliceOffice object with entirely undefined values.
     */
    public PoliceOffice(EntityID id) {
	super(id, EntityConstants.POLICE_OFFICE);
    }
}