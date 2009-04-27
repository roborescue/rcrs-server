package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityFactory;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Entity;

/**
   EntityFactory that builds standard Robocup Rescue objects.
 */
public class RescueEntityFactory implements EntityFactory {
    /**
       Singleton class. Use this instance to do stuff.
     */
    public static RescueEntityFactory INSTANCE = new RescueEntityFactory();

    /**
       Singleton class: private constructor.
     */
    private RescueEntityFactory() {}

    @Override
    public EntityType makeEntityType(int id) {
        switch (id) {
        case EntityConstants.TYPE_WORLD:
            return EntityConstants.WORLD;
        case EntityConstants.TYPE_ROAD:
            return EntityConstants.ROAD;
        case EntityConstants.TYPE_NODE:
            return EntityConstants.NODE;
        case EntityConstants.TYPE_BUILDING:
            return EntityConstants.BUILDING;
        case EntityConstants.TYPE_REFUGE:
            return EntityConstants.REFUGE;
        case EntityConstants.TYPE_FIRE_STATION:
            return EntityConstants.FIRE_STATION;
        case EntityConstants.TYPE_AMBULANCE_CENTRE:
            return EntityConstants.AMBULANCE_CENTRE;
        case EntityConstants.TYPE_POLICE_OFFICE:
            return EntityConstants.POLICE_OFFICE;
        case EntityConstants.TYPE_CIVILIAN:
            return EntityConstants.CIVILIAN;
        case EntityConstants.TYPE_FIRE_BRIGADE:
            return EntityConstants.FIRE_BRIGADE;
        case EntityConstants.TYPE_AMBULANCE_TEAM:
            return EntityConstants.AMBULANCE_TEAM;
        case EntityConstants.TYPE_POLICE_FORCE:
            return EntityConstants.POLICE_FORCE;
        default:
            throw new IllegalArgumentException("Unrecognised entity type: " + id);
        }
    }

    @Override
    public Entity makeEntity(EntityType type, EntityID id) {
        switch (type.getID()) {
        case EntityConstants.TYPE_WORLD:
            return new World(id);
        case EntityConstants.TYPE_ROAD:
            return new Road(id);
        case EntityConstants.TYPE_NODE:
            return new Node(id);
        case EntityConstants.TYPE_BUILDING:
            return new Building(id);
        case EntityConstants.TYPE_REFUGE:
            return new Refuge(id);
        case EntityConstants.TYPE_FIRE_STATION:
            return new FireStation(id);
        case EntityConstants.TYPE_AMBULANCE_CENTRE:
            return new AmbulanceCentre(id);
        case EntityConstants.TYPE_POLICE_OFFICE:
            return new PoliceOffice(id);
        case EntityConstants.TYPE_CIVILIAN:
            return new Civilian(id);
        case EntityConstants.TYPE_FIRE_BRIGADE:
            return new FireBrigade(id);
        case EntityConstants.TYPE_AMBULANCE_TEAM:
            return new AmbulanceTeam(id);
        case EntityConstants.TYPE_POLICE_FORCE:
            return new PoliceForce(id);
        default:
            throw new IllegalArgumentException("Unrecognised entity type: " + type);
        }
    }


    /**
       Construct a World object.
       @param id The ID of the new object.
       @return A new World object.
     */
    public Entity makeWorld(EntityID id) {
	return new World(id);
    }
}