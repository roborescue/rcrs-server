package rescuecore2.standard.entities;

import rescuecore2.worldmodel.EntityFactory;
import rescuecore2.worldmodel.EntityID;

/**
   EntityFactory that builds standard Robocup Standard objects.
 */
public final class StandardEntityFactory implements EntityFactory {
    /**
       Singleton class. Use this instance to do stuff.
     */
    public static final StandardEntityFactory INSTANCE = new StandardEntityFactory();

    /**
       Singleton class: private constructor.
     */
    private StandardEntityFactory() {}

    @Override
    public StandardEntity makeEntity(int typeID, EntityID id) {
        StandardEntityType type = StandardEntityType.fromID(typeID);
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
        return new int[] {StandardEntityType.WORLD.getID(),
                          StandardEntityType.ROAD.getID(),
                          StandardEntityType.NODE.getID(),
                          StandardEntityType.BUILDING.getID(),
                          StandardEntityType.REFUGE.getID(),
                          StandardEntityType.FIRE_STATION.getID(),
                          StandardEntityType.AMBULANCE_CENTRE.getID(),
                          StandardEntityType.POLICE_OFFICE.getID(),
                          StandardEntityType.CIVILIAN.getID(),
                          StandardEntityType.FIRE_BRIGADE.getID(),
                          StandardEntityType.AMBULANCE_TEAM.getID(),
                          StandardEntityType.POLICE_FORCE.getID()
        };
    }
}