package rescuecore2.standard.entities;

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
        super(id, StandardEntityURN.REFUGE);
    }

    /**
       Refuge copy constructor.
       @param other The Refuge to copy.
     */
    public Refuge(Refuge other) {
        super(other);
    }

    @Override
    protected Entity copyImpl() {
        return new Refuge(getID());
    }
}