package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityFactory;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Entity;

/**
   EntityFactory that builds standard Robocup Rescue objects.
 */
public class LegacyEntityFactory implements EntityFactory {
    /**
       Construct a World object.
       @param id The ID of the new object.
       @return A new World object.
     */
    public Entity makeWorld(EntityID id) {
	return new World(id);
    }
}