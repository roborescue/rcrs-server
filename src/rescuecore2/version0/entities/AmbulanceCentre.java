package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;

/**
   The AmbulanceCentre object.
 */
public class AmbulanceCentre extends Building {
    /**
       Construct a AmbulanceCentre object with entirely undefined values.
     */
    public AmbulanceCentre(EntityID id) {
	super(id, EntityConstants.AMBULANCE_CENTRE);
    }
}