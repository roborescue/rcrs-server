package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;

/**
   The Building object.
 */
public class Building extends AbstractLegacyEntity {
    /**
       Construct a Building object with entirely undefined values.
     */
    public Building(EntityID id) {
	this(EntityType.BUILDING, id);
    }

    /**
       Construct a subclass of a Building object with entirely undefined values.
     */
    protected Building(EntityType type, EntityID id) {
	super(type,
	      id,
	      new IntProperty(PropertyType.X.getName()),
	      new IntProperty(PropertyType.Y.getName()),
	      new IntProperty(PropertyType.FLOORS.getName()),
	      new BooleanProperty(PropertyType.IGNITION.getName()),
	      new IntProperty(PropertyType.FIERYNESS.getName()),
	      new IntProperty(PropertyType.BROKENNESS.getName()),
	      new IntProperty(PropertyType.BUILDING_CODE.getName()),
	      new IntProperty(PropertyType.BUILDING_ATTRIBUTES.getName()),
	      new IntProperty(PropertyType.BUILDING_AREA_GROUND.getName()),
	      new IntProperty(PropertyType.BUILDING_AREA_TOTAL.getName()),
	      new IntProperty(PropertyType.TEMPERATURE.getName()),
	      new IntProperty(PropertyType.IMPORTANCE.getName()),
	      new IntArrayProperty(PropertyType.BUILDING_APEXES.getName()),
	      new IntArrayProperty(PropertyType.ENTRANCES.getName())
	      );
    }
}