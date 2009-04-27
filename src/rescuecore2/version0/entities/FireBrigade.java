package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.version0.entities.properties.IntProperty;
import rescuecore2.version0.entities.properties.PropertyType;

/**
   FireBrigade class
 */
public class FireBrigade extends Human {
    public FireBrigade(EntityID id) {
        super(id, EntityConstants.FIRE_BRIGADE, new IntProperty(PropertyType.WATER_QUANTITY));
    }
}