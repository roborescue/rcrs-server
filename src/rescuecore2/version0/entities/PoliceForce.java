package rescuecore2.version0.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
   The PoliceForce object.
 */
public class PoliceForce extends Human {
    /**
       Construct a PoliceForce object with entirely undefined values.
       @param id The ID of this entity.
    */
    public PoliceForce(EntityID id) {
        super(id, EntityConstants.POLICE_FORCE);
    }

    @Override
    protected Entity copyImpl() {
        return new PoliceForce(getID());
    }
}