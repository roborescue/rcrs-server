package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.IntProperty;

/**
   The World object.
 */
public class World extends AbstractLegacyEntity {
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
     */
    public World(EntityID id) {
	super(EntityType.WORLD,
	      id,
	      new IntProperty(PropertyType.START_TIME.getName()),
	      new IntProperty(PropertyType.LONGITUDE.getName()),
	      new IntProperty(PropertyType.LATITUDE.getName()),
	      new IntProperty(PropertyType.WIND_FORCE.getName()),
	      new IntProperty(PropertyType.WIND_DIRECTION.getName()));
    }
}