package rescuecore2.version0.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
   The Refuge object.
 */
public class Refuge extends Building {
    /**
       Construct a Refuge object with entirely undefined values.
       @param id The ID of this entity.
     */
    public Refuge(EntityID id) {
        super(id, EntityConstants.REFUGE);
    }

    @Override
    protected Entity copyImpl() {
        return new Refuge(getID());
    }
}