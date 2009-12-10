package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;

/**
   The Road object.
 */
public class Road extends Area {
    /**
       Construct a Road object with entirely undefined property values.
       @param id The ID of this entity.
     */
    public Road(EntityID id) {
        super(id, StandardEntityURN.ROAD);
    }

    /**
       Road copy constructor.
       @param other The Road to copy.
     */
    public Road(Road other) {
        super(other);
    }

    @Override
    protected Entity copyImpl() {
        return new Road(getID());
    }
}