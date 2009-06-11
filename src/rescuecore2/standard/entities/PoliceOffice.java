package rescuecore2.standard.entities;

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
        super(id, StandardEntityType.POLICE_OFFICE);
    }

    @Override
    protected Entity copyImpl() {
        return new PoliceOffice(getID());
    }
}