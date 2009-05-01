package rescuecore2.worldmodel.entities.legacy;

/**
   A bunch of useful constants for entities.
 */
public enum EntityType {
    NULL(0),
        WORLD(0x01),
        ROAD(0x02),
        NODE(0x04),
        BUILDING(0x20),
        REFUGE(0x21),
        FIRE_STATION(0x22),
        AMBULANCE_CENTRE(0x23),
        POLICE_OFFICE(0x24),
        CIVILIAN(0x40),
        FIRE_BRIGADE(0x42),
        AMBULANCE_TEAM(0x43),
        POLICE_FORCE(0x44);

    private int id;

    private EntityType(int id) {
        this.id = id;
    }

    /**
       Get the ID number of this entity type.
       @return The ID of this entity type.
     */
    public int getID() {
        return id;
    }

    /**
       Look up an entity type by ID.
       @param id The ID to look up.
       @return The appropriate EntityType object.
       @throws IllegalArgumentException If the given id is not recognised.
     */
    public static EntityType fromID(int id) {
        for (EntityType next : EntityType.values()) {
            if (next.getID() == id) {
                return next;
            }
        }
        throw new IllegalArgumentException("Unrecognised entity type ID: " + id);
    }
}