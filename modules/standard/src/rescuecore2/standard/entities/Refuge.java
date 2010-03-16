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
        super(id);
    }

    /**
       Refuge copy constructor.
       @param other The Refuge to copy.
     */
    public Refuge(Refuge other) {
        super(other);
    }

    /**
       Create a refuge based on another Building.
       @param other The Building to copy.
     */
    public Refuge(Building other) {
        super(other);
    }

    @Override
    protected Entity copyImpl() {
        return new Refuge(getID());
    }

    @Override
    public StandardEntityURN getStandardURN() {
        return StandardEntityURN.REFUGE;
    }

    @Override
    protected String getEntityName() {
        return "Refuge";
    }
}
