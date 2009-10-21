package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
   The AmbulanceCentre object.
 */
public class AmbulanceCentre extends Building {
    /**
       Construct a AmbulanceCentre object with entirely undefined values.
       @param id The ID of this entity.
     */
    public AmbulanceCentre(EntityID id) {
        super(id, StandardEntityURN.AMBULANCE_CENTRE);
    }

    /**
       AmbulanceCentre copy constructor.
       @param other The AmbulanceCentre to copy.
     */
    public AmbulanceCentre(AmbulanceCentre other) {
        super(other);
    }

    @Override
    protected Entity copyImpl() {
        return new AmbulanceCentre(getID());
    }
}