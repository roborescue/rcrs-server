package rescuecore2.version0.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
   The PoliceOffice object.
 */
public class PoliceOffice extends Building {
    /**
       Construct a PoliceOffice object with entirely undefined property values.
       @param id The ID of this entity.
     */
    public PoliceOffice(EntityID id) {
        super(id, RescueEntityType.POLICE_OFFICE);
    }

    @Override
    protected Entity copyImpl() {
        return new PoliceOffice(getID());
    }
}