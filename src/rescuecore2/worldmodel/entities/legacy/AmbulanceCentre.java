package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityID;

/**
   The AmbulanceCentre object.
 */
public class AmbulanceCentre extends Building {
    /**
       Construct a AmbulanceCentre object with entirely undefined values.
     */
    public AmbulanceCentre(EntityID id) {
	super(EntityType.AMBULANCE_CENTRE, id);
    }
}