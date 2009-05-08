package rescuecore2.version0.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.version0.entities.properties.IntProperty;
import rescuecore2.version0.entities.properties.PropertyType;

/**
   The World object.
 */
public class World extends RescueObject {
    private IntProperty startTime;
    private IntProperty longitude;
    private IntProperty latitude;
    private IntProperty windForce;
    private IntProperty windDirection;

    /**
       Construct a World object with entirely undefined property values.
       @param id The ID of this entity.
    */
    public World(EntityID id) {
        super(id, EntityConstants.WORLD);
        startTime = new IntProperty(PropertyType.START_TIME);
        longitude = new IntProperty(PropertyType.LONGITUDE);
        latitude = new IntProperty(PropertyType.LATITUDE);
        windForce = new IntProperty(PropertyType.WIND_FORCE);
        windDirection = new IntProperty(PropertyType.WIND_DIRECTION);
        addProperties(startTime, longitude, latitude, windForce, windDirection);
    }

    @Override
    protected Entity copyImpl() {
        return new World(getID());
    }
}