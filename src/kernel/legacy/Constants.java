package kernel.legacy;

/**
   Useful constants for the kernel legacy stuff.
*/
public final class Constants {
    /** The agent request type for civilians. */
    public static final int AGENT_TYPE_CIVILIAN = 0x01;
    /** The agent request type for fire brigades. */
    public static final int AGENT_TYPE_FIRE_BRIGADE = 0x02;
    /** The agent request type for fire stations. */
    public static final int AGENT_TYPE_FIRE_STATION = 0x04;
    /** The agent request type for ambulance teams. */
    public static final int AGENT_TYPE_AMBULANCE_TEAM = 0x08;
    /** The agent request type for ambulance centres. */
    public static final int AGENT_TYPE_AMBULANCE_CENTRE = 0x10;
    /** The agent request type for police forces. */
    public static final int AGENT_TYPE_POLICE_FORCE = 0x20;
    /** The agent request type for police offices. */
    public static final int AGENT_TYPE_POLICE_OFFICE = 0x40;
    /** The agent request type for mobile agents (fire brigade, ambulance team, police force). */
    public static final int AGENT_TYPE_ANY_MOBILE = AGENT_TYPE_FIRE_BRIGADE | AGENT_TYPE_AMBULANCE_TEAM | AGENT_TYPE_POLICE_FORCE;
    /** The agent request type for buildings (fire station, ambulance centre, police office). */
    public static final int AGENT_TYPE_ANY_BUILDING = AGENT_TYPE_FIRE_STATION | AGENT_TYPE_AMBULANCE_CENTRE | AGENT_TYPE_POLICE_OFFICE;
    /** The agent request type for any kind of non-civilian agent. */
    public static final int AGENT_TYPE_ANY_AGENT = AGENT_TYPE_ANY_MOBILE | AGENT_TYPE_ANY_BUILDING;
    /** The agent request type for any kind of agent including civilians. */
    public static final int AGENT_TYPE_ANY = AGENT_TYPE_ANY_MOBILE | AGENT_TYPE_ANY_BUILDING | AGENT_TYPE_CIVILIAN;

    /** Utility class: no public constructor. */
    private Constants() {}
}