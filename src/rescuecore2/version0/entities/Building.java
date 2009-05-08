package rescuecore2.version0.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.version0.entities.properties.IntProperty;
import rescuecore2.version0.entities.properties.IntArrayProperty;
import rescuecore2.version0.entities.properties.BooleanProperty;
import rescuecore2.version0.entities.properties.PropertyType;
import rescuecore2.misc.Pair;

/**
   The Building object.
 */
public class Building extends RescueObject {
    private IntProperty x;
    private IntProperty y;
    private IntProperty floors;
    private BooleanProperty ignition;
    private IntProperty fieryness;
    private IntProperty brokenness;
    private IntProperty code;
    private IntProperty attributes;
    private IntProperty groundArea;
    private IntProperty totalArea;
    private IntProperty temperature;
    private IntProperty importance;
    private IntArrayProperty entrances;
    private IntArrayProperty apexes;

    /**
       Construct a Building object with entirely undefined property values.
       @param id The ID of this entity.
    */
    public Building(EntityID id) {
        this(id, EntityConstants.BUILDING);
    }

    /**
       Construct a subclass of a Building object with entirely undefined property values.
       @param id The ID of this entity.
       @param type The real type of this building.
    */
    protected Building(EntityID id, EntityType type) {
        super(id, type);
        x = new IntProperty(PropertyType.X);
        y = new IntProperty(PropertyType.Y);
        floors = new IntProperty(PropertyType.FLOORS);
        ignition = new BooleanProperty(PropertyType.IGNITION);
        fieryness = new IntProperty(PropertyType.FIERYNESS);
        brokenness = new IntProperty(PropertyType.BROKENNESS);
        code = new IntProperty(PropertyType.BUILDING_CODE);
        attributes = new IntProperty(PropertyType.BUILDING_ATTRIBUTES);
        groundArea = new IntProperty(PropertyType.BUILDING_AREA_GROUND);
        totalArea = new IntProperty(PropertyType.BUILDING_AREA_TOTAL);
        temperature = new IntProperty(PropertyType.TEMPERATURE);
        importance = new IntProperty(PropertyType.IMPORTANCE);
        apexes = new IntArrayProperty(PropertyType.BUILDING_APEXES);
        entrances = new IntArrayProperty(PropertyType.ENTRANCES);
        addProperties(x, y, floors, ignition, fieryness, brokenness, code, attributes, groundArea, totalArea, temperature, importance, apexes, entrances);
    }
    
    @Override
    protected Entity copyImpl() {
        return new Building(getID());
    }

    @Override
    public Pair<Integer, Integer> getLocation(WorldModel<? extends RescueObject> world) {
        return new Pair<Integer, Integer>(x.getValue(), y.getValue());
    }
}