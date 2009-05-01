package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.IntProperty;

/**
   FireBrigade class
 */
public class FireBrigade extends Human {
    public FireBrigade(EntityID id) {
	super(EntityType.FIRE_BRIGADE, id, new IntProperty(PropertyType.WATER_QUANTITY.getName()));
    }
}