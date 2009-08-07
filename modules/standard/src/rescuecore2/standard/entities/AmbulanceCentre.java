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
        super(id, StandardEntityType.AMBULANCE_CENTRE);
    }

    @Override
    protected Entity copyImpl() {
        return new AmbulanceCentre(getID());
    }
}