package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityType;

/**
   All types of entity available in version0.
 */
public enum RescueEntityType implements EntityType {
    // CHECKSTYLE:OFF:JavadocVariableCheck

    WORLD(0x01, "World"),
    ROAD(0x02, "Road"),
    NODE(0x04, "Node"),
    BUILDING(0x20, "Building"),
    REFUGE(0x21, "Refuge"),
    FIRE_STATION(0x22, "Fire station"),
    AMBULANCE_CENTRE(0x23, "Ambulance centre"),
    POLICE_OFFICE(0x24, "Police office"),
    CIVILIAN(0x40, "Civilian"),
    FIRE_BRIGADE(0x42, "Fire brigade"),
    AMBULANCE_TEAM(0x43, "Ambulance team"),
    POLICE_FORCE(0x44, "Police force");

    // CHECKSTYLE:ON:JavadocVariableCheck

    private int id;
    private String name;

    private RescueEntityType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
       Look up a type by ID.
       @param id The ID number to look up.
     */
    public static RescueEntityType fromID(int id) {
        for (RescueEntityType next : values()) {
            if (next.id == id) {
                return next;
            }
        }
        throw new IllegalArgumentException("Unrecognised entity type: " + id);
    }
}