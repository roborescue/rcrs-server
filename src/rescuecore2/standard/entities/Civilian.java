package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
   The Civilian object.
 */
public class Civilian extends Human {
    /**
       Construct a Civilian object with entirely undefined values.
       @param id The ID of this entity.
    */
    public Civilian(EntityID id) {
        super(id, StandardEntityType.CIVILIAN);
    }

    @Override
    protected Entity copyImpl() {
        return new Civilian(getID());
    }
}