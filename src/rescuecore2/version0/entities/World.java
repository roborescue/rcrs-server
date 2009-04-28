package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.version0.entities.properties.IntProperty;
import rescuecore2.version0.entities.properties.PropertyType;

/**
   The World object.
 */
public class World extends RescueObject {
    // Maybe cache the properties at some point? Do we really need to?
    /*
    private IntProperty startTime;
    private IntProperty longitude;
    private IntProperty latitude;
    private IntProperty windForce;
    private IntProperty windDirection;
    */

    /**
       Construct a World object with entirely undefined values.
       @param id The ID of this entity.
    */
    public World(EntityID id) {
        super(id,
              EntityConstants.WORLD,
              new IntProperty(PropertyType.START_TIME),
              new IntProperty(PropertyType.LONGITUDE),
              new IntProperty(PropertyType.LATITUDE),
              new IntProperty(PropertyType.WIND_FORCE),
              new IntProperty(PropertyType.WIND_DIRECTION));
    }
}