package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityType;

/**
   Entity constants for version 0 entities.
 */
public final class EntityConstants {
    public final static int TYPE_WORLD = 0x01;
    public final static EntityType WORLD = new EntityType(TYPE_WORLD, "World");
    public final static int TYPE_ROAD = 0x02;
    public final static EntityType ROAD = new EntityType(TYPE_ROAD, "Road");
    //    public final static int TYPE_RIVER = 0x03;
    //    public final static EntityType RIVER = new EntityType(TYPE_RIVER, "River");
    public final static int TYPE_NODE = 0x04;
    public final static EntityType NODE = new EntityType(TYPE_NODE, "Node");
    //    public final static int TYPE_RIVER_NODE = 0x05;
    //    public final static EntityType RIVER_NODE = new EntityType(TYPE_RIVER_NODE, "River node");
    public final static int TYPE_BUILDING = 0x20;
    public final static EntityType BUILDING = new EntityType(TYPE_BUILDING, "Building");
    public final static int TYPE_REFUGE = 0x21;
    public final static EntityType REFUGE = new EntityType(TYPE_REFUGE, "Refuge");
    public final static int TYPE_FIRE_STATION = 0x22;
    public final static EntityType FIRE_STATION = new EntityType(TYPE_FIRE_STATION, "Fire station");
    public final static int TYPE_AMBULANCE_CENTRE = 0x23;
    public final static EntityType AMBULANCE_CENTRE = new EntityType(TYPE_AMBULANCE_CENTRE, "Ambulance centre");
    public final static int TYPE_POLICE_OFFICE = 0x24;
    public final static EntityType POLICE_OFFICE = new EntityType(TYPE_POLICE_OFFICE, "Police office");
    public final static int TYPE_CIVILIAN = 0x40;
    public final static EntityType CIVILIAN = new EntityType(TYPE_CIVILIAN, "Civilian");
    //    public final static int TYPE_CAR = 0x41;
    //    public final static EntityType CAR = new EntityType(TYPE_CAR, "Car");
    public final static int TYPE_FIRE_BRIGADE = 0x42;
    public final static EntityType FIRE_BRIGADE = new EntityType(TYPE_FIRE_BRIGADE, "Fire brigade");
    public final static int TYPE_AMBULANCE_TEAM = 0x43;
    public final static EntityType AMBULANCE_TEAM = new EntityType(TYPE_AMBULANCE_TEAM, "Ambulance team");
    public final static int TYPE_POLICE_FORCE = 0x44;
    public final static EntityType POLICE_FORCE = new EntityType(TYPE_POLICE_FORCE, "Police force");

    /**
       Utility class: private constructor.
    */
    private EntityConstants() {}
}