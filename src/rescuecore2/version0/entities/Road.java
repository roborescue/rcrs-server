package rescuecore2.version0.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.version0.entities.properties.IntProperty;
import rescuecore2.version0.entities.properties.BooleanProperty;
import rescuecore2.version0.entities.properties.PropertyType;

/**
   The Road object.
 */
public class Road extends Edge {
    /**
       Construct a Road object with entirely undefined values.
       @param id The ID of this entity.
     */
    public Road(EntityID id) {
        super(id,
              EntityConstants.ROAD,
              // Road properties
              new IntProperty(PropertyType.ROAD_KIND),
              new IntProperty(PropertyType.CARS_PASS_TO_HEAD),
              new IntProperty(PropertyType.CARS_PASS_TO_TAIL),
              new IntProperty(PropertyType.HUMANS_PASS_TO_HEAD),
              new IntProperty(PropertyType.HUMANS_PASS_TO_TAIL),
              new IntProperty(PropertyType.WIDTH),
              new IntProperty(PropertyType.BLOCK),
              new IntProperty(PropertyType.REPAIR_COST),
              new BooleanProperty(PropertyType.MEDIAN_STRIP),
              new IntProperty(PropertyType.LINES_TO_HEAD),
              new IntProperty(PropertyType.LINES_TO_TAIL),
              new IntProperty(PropertyType.WIDTH_FOR_WALKERS),
              // Edge properties
              new IntProperty(PropertyType.HEAD),
              new IntProperty(PropertyType.TAIL),
              new IntProperty(PropertyType.LENGTH)
              );
    }

    @Override
    protected Entity copyImpl() {
        return new Road(getID());
    }
}