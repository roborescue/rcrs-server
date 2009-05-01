package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;

/**
   Abstract base class for Humans.
 */
public abstract class Human extends AbstractLegacyEntity {
    /**
       Construct a subclass of a Human object with entirely undefined values.
    */
    protected Human(EntityType type, EntityID id) {
	super(type,
	      id,
	      new IntProperty(PropertyType.POSITION.getName()),
	      new IntProperty(PropertyType.POSITION_EXTRA.getName()),
	      new IntArrayProperty(PropertyType.POSITION_HISTORY.getName()),
	      new IntProperty(PropertyType.DIRECTION.getName()),
	      new IntProperty(PropertyType.STAMINA.getName()),
	      new IntProperty(PropertyType.HP.getName()),
	      new IntProperty(PropertyType.DAMAGE.getName()),
	      new IntProperty(PropertyType.BURIEDNESS.getName())
	      );
    }

    /**
       Construct a subclass of a Human object with entirely undefined values.
    */
    protected Human(EntityType type, EntityID id, Property extraProp) {
	super(type,
	      id,
	      new IntProperty(PropertyType.POSITION.getName()),
	      new IntProperty(PropertyType.POSITION_EXTRA.getName()),
	      new IntArrayProperty(PropertyType.POSITION_HISTORY.getName()),
	      new IntProperty(PropertyType.DIRECTION.getName()),
	      new IntProperty(PropertyType.STAMINA.getName()),
	      new IntProperty(PropertyType.HP.getName()),
	      new IntProperty(PropertyType.DAMAGE.getName()),
	      new IntProperty(PropertyType.BURIEDNESS.getName()),
	      extraProp);
    }
}