package kernel.legacy;

/**
   Useful constants for the kernel legacy stuff.
*/
public final class Constants {
    public final static int AGENT_TYPE_CIVILIAN = 0x01;
    public final static int AGENT_TYPE_FIRE_BRIGADE = 0x02;
    public final static int AGENT_TYPE_FIRE_STATION = 0x04;
    public final static int AGENT_TYPE_AMBULANCE_TEAM = 0x08;
    public final static int AGENT_TYPE_AMBULANCE_CENTRE = 0x10;
    public final static int AGENT_TYPE_POLICE_FORCE = 0x20;
    public final static int AGENT_TYPE_POLICE_OFFICE = 0x40;
    public final static int AGENT_TYPE_ANY_MOBILE = AGENT_TYPE_FIRE_BRIGADE | AGENT_TYPE_AMBULANCE_TEAM | AGENT_TYPE_POLICE_FORCE;
    public final static int AGENT_TYPE_ANY_BUILDING = AGENT_TYPE_FIRE_STATION | AGENT_TYPE_AMBULANCE_CENTRE | AGENT_TYPE_POLICE_OFFICE;
    public final static int AGENT_TYPE_ANY_AGENT = AGENT_TYPE_ANY_MOBILE | AGENT_TYPE_ANY_BUILDING;
    public final static int AGENT_TYPE_ANY = AGENT_TYPE_ANY_MOBILE | AGENT_TYPE_ANY_BUILDING | AGENT_TYPE_CIVILIAN;

    /** Utility class: no public constructor */
    private Constants() {}
}