package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.Property;
import rescuecore2.version0.entities.properties.IntProperty;
import rescuecore2.version0.entities.properties.IntArrayProperty;
import rescuecore2.version0.entities.properties.PropertyType;

/**
   Abstract base class for Humans.
 */
public abstract class Human extends RescueObject {
    /**
       Construct a subclass of a Human object with entirely undefined values.
    */
    protected Human(EntityID id, EntityType type) {
	super(id,
              type,
	      new IntProperty(PropertyType.POSITION),
	      new IntProperty(PropertyType.POSITION_EXTRA),
	      new IntArrayProperty(PropertyType.POSITION_HISTORY),
	      new IntProperty(PropertyType.DIRECTION),
	      new IntProperty(PropertyType.STAMINA),
	      new IntProperty(PropertyType.HP),
	      new IntProperty(PropertyType.DAMAGE),
	      new IntProperty(PropertyType.BURIEDNESS)
	      );
    }

    /**
       Construct a subclass of a Human object with entirely undefined values.
    */
    protected Human(EntityID id, EntityType type, Property extraProp) {
	super(id,
              type,
	      new IntProperty(PropertyType.POSITION),
	      new IntProperty(PropertyType.POSITION_EXTRA),
	      new IntArrayProperty(PropertyType.POSITION_HISTORY),
	      new IntProperty(PropertyType.DIRECTION),
	      new IntProperty(PropertyType.STAMINA),
	      new IntProperty(PropertyType.HP),
	      new IntProperty(PropertyType.DAMAGE),
	      new IntProperty(PropertyType.BURIEDNESS),
	      extraProp);
    }
}