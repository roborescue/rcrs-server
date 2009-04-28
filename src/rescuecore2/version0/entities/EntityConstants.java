package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityType;

/**
   Entity constants for version 0 entities.
 */
public final class EntityConstants {
    /** The world type ID. */
    public static final int TYPE_WORLD = 0x01;
    /** The world EntityType. */
    public static final EntityType WORLD = new EntityType(TYPE_WORLD, "World");
    /** The road type ID. */
    public static final int TYPE_ROAD = 0x02;
    /** The road EntityType. */
    public static final EntityType ROAD = new EntityType(TYPE_ROAD, "Road");
    /** The river type ID. */
    //    public static final int TYPE_RIVER = 0x03;
    /** The river EntityType. */
    //    public static final EntityType RIVER = new EntityType(TYPE_RIVER, "River");
    /** The node type ID. */
    public static final int TYPE_NODE = 0x04;
    /** The node EntityType. */
    public static final EntityType NODE = new EntityType(TYPE_NODE, "Node");
    /** The river node type ID. */
    //    public static final int TYPE_RIVER_NODE = 0x05;
    /** The river node EntityType. */
    //    public static final EntityType RIVER_NODE = new EntityType(TYPE_RIVER_NODE, "River node");
    /** The building type ID. */
    public static final int TYPE_BUILDING = 0x20;
    /** The building EntityType. */
    public static final EntityType BUILDING = new EntityType(TYPE_BUILDING, "Building");
    /** The refuge type ID. */
    public static final int TYPE_REFUGE = 0x21;
    /** The refuge EntityType. */
    public static final EntityType REFUGE = new EntityType(TYPE_REFUGE, "Refuge");
    /** The fire station type ID. */
    public static final int TYPE_FIRE_STATION = 0x22;
    /** The fire station EntityType. */
    public static final EntityType FIRE_STATION = new EntityType(TYPE_FIRE_STATION, "Fire station");
    /** The ambulance centre type ID. */
    public static final int TYPE_AMBULANCE_CENTRE = 0x23;
    /** The ambulance centre EntityType. */
    public static final EntityType AMBULANCE_CENTRE = new EntityType(TYPE_AMBULANCE_CENTRE, "Ambulance centre");
    /** The police office type ID. */
    public static final int TYPE_POLICE_OFFICE = 0x24;
    /** The police office EntityType. */
    public static final EntityType POLICE_OFFICE = new EntityType(TYPE_POLICE_OFFICE, "Police office");
    /** The civilian type ID. */
    public static final int TYPE_CIVILIAN = 0x40;
    /** The civilian EntityType. */
    public static final EntityType CIVILIAN = new EntityType(TYPE_CIVILIAN, "Civilian");
    /** The car type ID. */
    //    public static final int TYPE_CAR = 0x41;
    /** The car EntityType. */
    //    public static final EntityType CAR = new EntityType(TYPE_CAR, "Car");
    /** The fire brigade type ID. */
    public static final int TYPE_FIRE_BRIGADE = 0x42;
    /** The fire brigade EntityType. */
    public static final EntityType FIRE_BRIGADE = new EntityType(TYPE_FIRE_BRIGADE, "Fire brigade");
    /** The ambulance team type ID. */
    public static final int TYPE_AMBULANCE_TEAM = 0x43;
    /** The ambulance team EntityType. */
    public static final EntityType AMBULANCE_TEAM = new EntityType(TYPE_AMBULANCE_TEAM, "Ambulance team");
    /** The police force type ID. */
    public static final int TYPE_POLICE_FORCE = 0x44;
    /** The police force EntityType. */
    public static final EntityType POLICE_FORCE = new EntityType(TYPE_POLICE_FORCE, "Police force");

    /**
       Utility class: private constructor.
    */
    private EntityConstants() {}
}