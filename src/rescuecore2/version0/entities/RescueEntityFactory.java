package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityFactory;
import rescuecore2.worldmodel.EntityID;

/**
   EntityFactory that builds standard Robocup Rescue objects.
 */
public final class RescueEntityFactory implements EntityFactory {
    /**
       Singleton class. Use this instance to do stuff.
     */
    public static final RescueEntityFactory INSTANCE = new RescueEntityFactory();

    /**
       Singleton class: private constructor.
     */
    private RescueEntityFactory() {}

    /*
    @Override
    public RescueEntityType makeEntityType(int id) {
        return RescueEntityType.fromID(id);
    }
    */

    @Override
    public RescueEntity makeEntity(int typeID, EntityID id) {
        RescueEntityType type = RescueEntityType.fromID(typeID);
        switch (type) {
        case WORLD:
            return new World(id);
        case ROAD:
            return new Road(id);
        case NODE:
            return new Node(id);
        case BUILDING:
            return new Building(id);
        case REFUGE:
            return new Refuge(id);
        case FIRE_STATION:
            return new FireStation(id);
        case AMBULANCE_CENTRE:
            return new AmbulanceCentre(id);
        case POLICE_OFFICE:
            return new PoliceOffice(id);
        case CIVILIAN:
            return new Civilian(id);
        case FIRE_BRIGADE:
            return new FireBrigade(id);
        case AMBULANCE_TEAM:
            return new AmbulanceTeam(id);
        case POLICE_FORCE:
            return new PoliceForce(id);
        default:
            throw new IllegalArgumentException("Unrecognised entity type: " + type);
        }
    }

    @Override
    public int[] getKnownEntityTypeIDs() {
        return new int[] {RescueEntityType.WORLD.getID(),
                          RescueEntityType.ROAD.getID(),
                          RescueEntityType.NODE.getID(),
                          RescueEntityType.BUILDING.getID(),
                          RescueEntityType.REFUGE.getID(),
                          RescueEntityType.FIRE_STATION.getID(),
                          RescueEntityType.AMBULANCE_CENTRE.getID(),
                          RescueEntityType.POLICE_OFFICE.getID(),
                          RescueEntityType.CIVILIAN.getID(),
                          RescueEntityType.FIRE_BRIGADE.getID(),
                          RescueEntityType.AMBULANCE_TEAM.getID(),
                          RescueEntityType.POLICE_FORCE.getID()
        };
    }
}