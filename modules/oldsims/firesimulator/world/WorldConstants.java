package firesimulator.world;

/**
 * @author tn
 *
 */
public interface WorldConstants {
	
    public final static int TYPE_NULL = 0;
    public final static int TYPE_WORLD = 0x01;
    public final static int TYPE_ROAD = 0x02;
    public final static int TYPE_RIVER = 0x03;
    public final static int TYPE_NODE = 0x04;
    public final static int TYPE_RIVER_NODE = 0x05;
    public final static int TYPE_BUILDING = 0x20;
    public final static int TYPE_REFUGE = 0x21;
    public final static int TYPE_FIRE_STATION = 0x22;
    public final static int TYPE_AMBULANCE_CENTER = 0x23;
    public final static int TYPE_POLICE_OFFICE = 0x24;
    public final static int TYPE_CIVILIAN = 0x40;
    public final static int TYPE_CAR = 0x41;
    public final static int TYPE_FIRE_BRIGADE = 0x42;
    public final static int TYPE_AMBULANCE_TEAM = 0x43;
    public final static int TYPE_POLICE_FORCE = 0x44;

	public final static int PROPERTY_NULL = 0;
	public final static int PROPERTY_MIN = 1;
	public final static int PROPERTY_START_TIME = 1;
	public final static int PROPERTY_LONGITUDE = 2;
	public final static int PROPERTY_LATITUDE = 3;
	public final static int PROPERTY_WIND_FORCE = 4;
	public final static int PROPERTY_WIND_DIRECTION = 5;

	public final static int PROPERTY_HEAD = 6;
	public final static int PROPERTY_TAIL = 7;
	public final static int PROPERTY_LENGTH = 8;

	public final static int PROPERTY_ROAD_KIND = 9;
	public final static int PROPERTY_CARS_PASS_TO_HEAD = 10;
	public final static int PROPERTY_CARS_PASS_TO_TAIL = 11;
	public final static int PROPERTY_HUMANS_PASS_TO_HEAD = 12;
	public final static int PROPERTY_HUMANS_PASS_TO_TAIL = 13;
	public final static int PROPERTY_WIDTH = 14;
	public final static int PROPERTY_BLOCK = 15;
	public final static int PROPERTY_REPAIR_COST = 16;
	public final static int PROPERTY_MEDIAN_STRIP = 17;
	public final static int PROPERTY_LINES_TO_HEAD = 18;
	public final static int PROPERTY_LINES_TO_TAIL = 19;
	public final static int PROPERTY_WIDTH_FOR_WALKERS = 20;
	public final static int PROPERTY_SIGNAL = 21;
	public final static int PROPERTY_SHORTCUT_TO_TURN = 22;
	public final static int PROPERTY_POCKET_TO_TURN_ACROSS = 23;
	public final static int PROPERTY_SIGNAL_TIMING = 24;

	public final static int PROPERTY_X = 25;
	public final static int PROPERTY_Y = 26;
	public final static int PROPERTY_EDGES = 27;

	public final static int PROPERTY_FLOORS = 28;
	public final static int PROPERTY_BUILDING_ATTRIBUTES = 29;
	public final static int PROPERTY_IGNITION = 30;
	public final static int PROPERTY_FIERYNESS = 31;
	public final static int PROPERTY_BROKENNESS = 32;
	public final static int PROPERTY_ENTRANCES = 33;
	public final static int PROPERTY_BUILDING_CODE = 34;
	public final static int PROPERTY_BUILDING_AREA_GROUND = 35;
	public final static int PROPERTY_BUILDING_AREA_TOTAL = 36;
	public final static int PROPERTY_BUILDING_APEXES = 37;

	public final static int PROPERTY_POSITION = 38;
	public final static int PROPERTY_POSITION_EXTRA = 39;
	public final static int PROPERTY_DIRECTION = 40;
	public final static int PROPERTY_POSITION_HISTORY = 41;
	public final static int PROPERTY_STAMINA = 42;
	public final static int PROPERTY_HP = 43;
	public final static int PROPERTY_DAMAGE = 44;
	public final static int PROPERTY_BURIEDNESS = 45;
	public final static int PROPERTY_WATER_QUANTITY = 46;

	public final static int PROPERTY_MAX = 46;
	
	static final int AK_EXTINGUISH 										= 0x86;
	static final int AK_REST       											= 0x80;
	static final int AK_MOVE       											= 0x81;
	static final int AK_LOAD       											= 0x82;
	static final int AK_UNLOAD     											= 0x83;
	static final int AK_SAY        												= 0x84;
	static final int AK_TELL       												= 0x85;	
	//	static final int AK_STRETCH    											= 0x87;
	static final int AK_RESCUE     											= 0x88;
	static final int AK_CLEAR      											= 0x89;
}
