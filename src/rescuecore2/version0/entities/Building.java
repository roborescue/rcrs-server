package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.version0.entities.properties.IntProperty;
import rescuecore2.version0.entities.properties.IntArrayProperty;
import rescuecore2.version0.entities.properties.BooleanProperty;
import rescuecore2.version0.entities.properties.PropertyType;

/**
   The Building object.
 */
public class Building extends RescueObject {
    /**
       Construct a Building object with entirely undefined values.
       @param id The ID of this entity.
    */
    public Building(EntityID id) {
        this(id, EntityConstants.BUILDING);
    }

    /**
       Construct a subclass of a Building object with entirely undefined values.
       @param id The ID of this entity.
       @param type The real type of this building.
    */
    protected Building(EntityID id, EntityType type) {
        super(id,
              type,
              new IntProperty(PropertyType.X),
              new IntProperty(PropertyType.Y),
              new IntProperty(PropertyType.FLOORS),
              new BooleanProperty(PropertyType.IGNITION),
              new IntProperty(PropertyType.FIERYNESS),
              new IntProperty(PropertyType.BROKENNESS),
              new IntProperty(PropertyType.BUILDING_CODE),
              new IntProperty(PropertyType.BUILDING_ATTRIBUTES),
              new IntProperty(PropertyType.BUILDING_AREA_GROUND),
              new IntProperty(PropertyType.BUILDING_AREA_TOTAL),
              new IntProperty(PropertyType.TEMPERATURE),
              new IntProperty(PropertyType.IMPORTANCE),
              new IntArrayProperty(PropertyType.BUILDING_APEXES),
              new IntArrayProperty(PropertyType.ENTRANCES)
              );
    }
}