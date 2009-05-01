package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;

/**
   The Road object.
 */
public class Road extends Edge {
    /**
       Construct a Road object with entirely undefined values.
     */
    public Road(EntityID id) {
	super(EntityType.ROAD,
	      id,
	      // Road properties
	      new IntProperty(PropertyType.ROAD_KIND.getName()),
	      new IntProperty(PropertyType.CARS_PASS_TO_HEAD.getName()),
	      new IntProperty(PropertyType.CARS_PASS_TO_TAIL.getName()),
	      new IntProperty(PropertyType.HUMANS_PASS_TO_HEAD.getName()),
	      new IntProperty(PropertyType.HUMANS_PASS_TO_TAIL.getName()),
	      new IntProperty(PropertyType.WIDTH.getName()),
	      new IntProperty(PropertyType.BLOCK.getName()),
	      new IntProperty(PropertyType.REPAIR_COST.getName()),
	      new BooleanProperty(PropertyType.MEDIAN_STRIP.getName()),
	      new IntProperty(PropertyType.LINES_TO_HEAD.getName()),
	      new IntProperty(PropertyType.LINES_TO_TAIL.getName()),
	      new IntProperty(PropertyType.WIDTH_FOR_WALKERS.getName()),
	      // Edge properties
	      new IntProperty(PropertyType.HEAD.getName()),
	      new IntProperty(PropertyType.TAIL.getName()),
	      new IntProperty(PropertyType.LENGTH.getName())
	      );
    }
}