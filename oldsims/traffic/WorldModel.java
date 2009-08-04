package traffic;
import java.util.*;
import traffic.object.*;
import rescuecore.InputBuffer;
import rescuecore.OutputBuffer;
import rescuecore.RescueConstants;

public class WorldModel implements Constants {
    public final HashMap idObjMap = new HashMap();
    private ArrayList m_movingObjectList = new ArrayList();
    private MovingObject[] m_movingObjectArray;
    public final ArrayList ambulanceTeamList = new ArrayList();
    public final ArrayList fireBrigadeList = new ArrayList();
    private int m_minX = Integer.MAX_VALUE;
    private int m_minY = Integer.MAX_VALUE;
    private int m_maxX = Integer.MIN_VALUE;
    private int m_maxY = Integer.MIN_VALUE;
    private int m_time = Integer.MIN_VALUE;;
    private double m_sec;
    private boolean m_isInitialized = false;

    public MovingObject[] movingObjectArray() {
        return m_movingObjectArray;
    }

    public int minX() {
        return m_minX;
    }

    public int maxX() {
        return m_maxX;
    }

    public int minY() {
        return m_minY;
    }

    public int maxY() {
        return m_maxY;
    }

    public int time() {
        return m_time;
    }

    public double sec() {
        return m_sec;
    }

    public double secTime() {
        return m_time * 60d + m_sec;
    }

    public boolean isInitialized() {
        return m_isInitialized;
    }

    public void setSec(double sec) {
        m_sec = sec;
    }

    public void progressSec() {
        m_sec += UNIT_SEC;
    }

    public void add(RealObject obj) {
        idObjMap.put(new Integer(obj.id), obj);
        if (obj instanceof MovingObject) {
            m_movingObjectList.add(obj);
            if (obj instanceof AmbulanceTeam)
                ambulanceTeamList.add(obj);
            if (obj instanceof FireBrigade)
                fireBrigadeList.add(obj);           
        }
    }

    public RealObject get(int id) {
        return (RealObject) idObjMap.get(new Integer(id));
    }

    public void update(InputBuffer in, int time) {
        m_time = time;
        int count = in.readInt();
        for (int i = 0; i < count; ++i) {
            int type = in.readInt();
            if (type==RescueConstants.TYPE_NULL) return;
            int id = in.readInt();
            int size = in.readInt();
            RescueObject obj = get(id);
            if (obj == null) {
                obj = newRescueObject(type, id);
                if (obj instanceof RealObject)
                    add((RealObject) obj);
            }
            int prop;
            while ((prop = in.readInt()) != RescueConstants.PROPERTY_NULL) {
                setProperty(prop, in, obj);
            }
        }
    }

    private RescueObject newRescueObject(int type, int id) {
        RescueObject obj;
        switch (type) {
		default :
			if (ASSERT)
				Util.myassert(false, "illeagle object type", type);
		case RescueConstants.TYPE_WORLD :
			obj = new World(id);
			break;
		case RescueConstants.TYPE_RIVER :
			obj = new River(id);
			break;
		case RescueConstants.TYPE_RIVER_NODE :
			obj = new RiverNode(id);
			break;
		case RescueConstants.TYPE_ROAD :
			obj = new Road(id);
			break;
		case RescueConstants.TYPE_NODE :
			obj = new Node(id);
			break;
		case RescueConstants.TYPE_BUILDING :
			obj = new Building(id);
			break;
		case RescueConstants.TYPE_AMBULANCE_CENTER :
			obj = new AmbulanceCenter(id);
			break;
		case RescueConstants.TYPE_FIRE_STATION :
			obj = new FireStation(id);
			break;
		case RescueConstants.TYPE_POLICE_OFFICE :
			obj = new PoliceOffice(id);
			break;
		case RescueConstants.TYPE_REFUGE :
			obj = new Refuge(id);
			break;
		case RescueConstants.TYPE_CIVILIAN :
			obj = new Civilian(id);
			break;
		case RescueConstants.TYPE_AMBULANCE_TEAM :
			obj = new AmbulanceTeam(id);
			break;
		case RescueConstants.TYPE_FIRE_BRIGADE :
			obj = new FireBrigade(id);
			break;
		case RescueConstants.TYPE_POLICE_FORCE :
			obj = new PoliceForce(id);
			break;
		case RescueConstants.TYPE_CAR :
			obj = new Car(id);
			break;
        }
        return obj;
    }

    private void setProperty(int property, InputBuffer data, RescueObject obj) {
		int size = data.readInt();
		int[] val = null;
		switch (property) {
		case RescueConstants.PROPERTY_START_TIME:
		case RescueConstants.PROPERTY_LONGITUDE:
		case RescueConstants.PROPERTY_LATITUDE:
		case RescueConstants.PROPERTY_WIND_FORCE:
		case RescueConstants.PROPERTY_WIND_DIRECTION:
		case RescueConstants.PROPERTY_X:
		case RescueConstants.PROPERTY_Y:
		case RescueConstants.PROPERTY_DIRECTION:
		case RescueConstants.PROPERTY_POSITION:
		case RescueConstants.PROPERTY_POSITION_EXTRA:
		case RescueConstants.PROPERTY_STAMINA:
		case RescueConstants.PROPERTY_HP:
		case RescueConstants.PROPERTY_DAMAGE:
		case RescueConstants.PROPERTY_BURIEDNESS:
		case RescueConstants.PROPERTY_FLOORS:
		case RescueConstants.PROPERTY_BUILDING_ATTRIBUTES:
		case RescueConstants.PROPERTY_IGNITION:
		case RescueConstants.PROPERTY_BROKENNESS:
		case RescueConstants.PROPERTY_FIERYNESS:
			//		case RescueConstants.PROPERTY_BUILDING_SHAPE_ID:
		case RescueConstants.PROPERTY_BUILDING_CODE:
		case RescueConstants.PROPERTY_BUILDING_AREA_GROUND:
		case RescueConstants.PROPERTY_BUILDING_AREA_TOTAL:
		case RescueConstants.PROPERTY_WATER_QUANTITY:
			//		case RescueConstants.PROPERTY_STRETCHED_LENGTH:
		case RescueConstants.PROPERTY_HEAD:
		case RescueConstants.PROPERTY_TAIL:
		case RescueConstants.PROPERTY_LENGTH:
		case RescueConstants.PROPERTY_ROAD_KIND:
		case RescueConstants.PROPERTY_CARS_PASS_TO_HEAD:
		case RescueConstants.PROPERTY_CARS_PASS_TO_TAIL:
		case RescueConstants.PROPERTY_HUMANS_PASS_TO_HEAD:
		case RescueConstants.PROPERTY_HUMANS_PASS_TO_TAIL:
		case RescueConstants.PROPERTY_WIDTH:
		case RescueConstants.PROPERTY_BLOCK:
		case RescueConstants.PROPERTY_REPAIR_COST:
		case RescueConstants.PROPERTY_MEDIAN_STRIP:
		case RescueConstants.PROPERTY_LINES_TO_HEAD:
		case RescueConstants.PROPERTY_LINES_TO_TAIL:
		case RescueConstants.PROPERTY_WIDTH_FOR_WALKERS:
		case RescueConstants.PROPERTY_SIGNAL:
		case RescueConstants.PROPERTY_BUILDING_IMPORTANCE:
		case RescueConstants.PROPERTY_BUILDING_TEMPERATURE:
			val = new int[] {data.readInt()};
			break;
		case RescueConstants.PROPERTY_EDGES:
		case RescueConstants.PROPERTY_SIGNAL_TIMING:
		case RescueConstants.PROPERTY_SHORTCUT_TO_TURN:
		case RescueConstants.PROPERTY_POCKET_TO_TURN_ACROSS:
		case RescueConstants.PROPERTY_POSITION_HISTORY:
		case RescueConstants.PROPERTY_ENTRANCES:
		case RescueConstants.PROPERTY_BUILDING_APEXES:
			val = new int[data.readInt()];
			for (int i=0;i<val.length;++i) val[i] = data.readInt();
			break;
		default:
			System.err.println("Unrecognised property: "+property);
			data.skip(size);
			break;
		}
		if (val!=null)
			obj.input(property,val);
    }

    public void shuffleMvObjsArray() {
        MovingObject tmp;
        MovingObject[] mvs = m_movingObjectArray;
        for (int i = mvs.length - 1; i > 0; i--) {
            tmp = mvs[i];
            int exchangeIndex = RANDOM.nextInt(i);
            mvs[i] = mvs[exchangeIndex];
            mvs[exchangeIndex] = tmp;
        }
    }

    public void initialize() {
        m_isInitialized = true;
        m_movingObjectArray = (MovingObject[]) m_movingObjectList
			.toArray(new MovingObject[m_movingObjectList.size()]);
        m_movingObjectList = null;
        preCulcLanesAndRoadList();
    }

    private void preCulcLanesAndRoadList() {
        Iterator it = idObjMap.values().iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof PointObject) {
                PointObject po = (PointObject) obj;
                po.outLanes();
                po.inLanes();
                if (USE_VIEWER) {
                    if (m_minX > po.x())
                        m_minX = po.x();
                    if (m_maxX < po.x())
                        m_maxX = po.x();
                    if (m_minY > po.y())
                        m_minY = po.y();
                    if (m_maxY < po.y())
                        m_maxY = po.y();
                }
            }
            else if (obj instanceof Road) {
                ((Road) obj).setAdjacentLanesOfSamePriorityRoad();
            }
        }
        it = idObjMap.values().iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof Road) {
                Road rd = (Road) obj;
                RoadCell.roadList((Lane) rd.lanesToHead().get(0));
                RoadCell.roadList((Lane) rd.lanesToTail().get(0));
            }
        }
    }

    public void parseCommands(InputBuffer in) {
        m_time = in.readInt();
        int count = in.readInt();
        for (int i = 0;i < count; ++i) {
            int command = in.readInt();
            int size = in.readInt();
			switch (command) {
			case RescueConstants.AK_MOVE:
				parseAK_MOVE(in);
				break;
			case RescueConstants.AK_LOAD:
				parseAK_LOAD(in);
				break;
			case RescueConstants.AK_UNLOAD:
				parseAK_UNLOAD(in);
				break;
			default:
				in.skip(size);
				break;
			}
        }
    }

	/*
    private void parseAK_MOVEs(InputBuffer in) {
		int count = in.readInt();
		for (int i=0;i<count;++i)
			parseAK_MOVE(in);
    }
	*/

    private void parseAK_MOVE(InputBuffer in) {
		//		int size = in.readInt();
        int senderID = in.readInt();
        int time = in.readInt();
		int length = in.readInt();
		int[] path = new int[length];
		for (int i=0;i<length;++i) path[i] = in.readInt();
        RealObject sender = get(senderID);
        if (!(sender instanceof Humanoid)) {
            printError(sender, "Wrong sender for moving: " + senderID);
			return;
        }
        Humanoid agent = (Humanoid) sender;
		//		System.out.println("Agent "+agent.id+" trying to move. Buriedness="+agent.buriedness());
		if (agent.buriedness()>0) {
			System.out.println("Ignoring move from buried agent "+agent.id+" (buriedness="+agent.buriedness());
			return;
		}
        agent.setLastMovingTime(m_time);
        Route submitedRoute = new Route(length);
        for (int i = 0; i < length; i++)
            submitedRoute.add((MotionlessObject) get(path[i]));
        agent.setRoutePlan(submitedRoute.checkValidity(agent) ? submitedRoute : Route
						   .singleObjRoute(agent.motionlessPosition()));
    }

	/*
    private void parseAK_LOADs(InputBuffer in) {
		int count = in.readInt();
		for (int i=0;i<count;++i)
			parseAK_LOAD(in);
    }
	*/

    private void parseAK_LOAD(InputBuffer in) {
		//        int size = in.readInt();
        int senderID = in.readInt();
        int time = in.readInt();
		int targetID = in.readInt();
        RealObject sender = get(senderID);
        if (!(sender instanceof AmbulanceTeam)) {
            printError(sender, "Wrong sender for loading: " + senderID);
            return;
        }
        AmbulanceTeam ambulance = (AmbulanceTeam) sender;
        Object target = get(targetID);
        if (!(target instanceof Humanoid)) {
            printError(sender, "Wrong loading target: " + target);
            return;
        }
        if (ambulance.isLoading()) {
            printError(sender, "This AmbulanceTeam has been loading an injured person already");
            return;
        }
        Humanoid mv = (Humanoid) target;
		if (mv.buriedness()>0) {
			printError(sender,"The target "+target+" is still buried: "+mv.buriedness());
		}
        if (ambulance.position() == mv.position()
			|| (!(mv.position() instanceof Building) && (ambulance.position() instanceof Node
														 && ((Node) ambulance.position()).edges().contains(mv.position()) || ambulance
														 .position() instanceof Road
														 && (((Road) ambulance.position()).head() == mv.position() || ((Road) ambulance
																													   .position()).tail() == mv.position())))) {
            ambulance.setLoad(mv);
        }
        else {
            printError(sender, "Wrong position: This AmbulanceTeam's position ("
					   + ambulance.position().id + ") is not the same position of the target's ("
					   + mv.position().id + ").");
        }
        return;
    }

	/*
    private void parseAK_UNLOADs(InputBuffer in) {
		int count = in.readInt();
		for (int i=0;i<count;++i)
			parseAK_UNLOAD(in);
    }
	*/

    private void parseAK_UNLOAD(InputBuffer in) {
		//        int size = in.readInt();
        int senderID = in.readInt();
        int time = in.readInt();
        RealObject sender = get(senderID);
        if (!(sender instanceof AmbulanceTeam)) {
            printError(sender, "Wrong sender for unloading: " + senderID);
            return;
        }
        AmbulanceTeam ambulance = (AmbulanceTeam) sender;
        if (ambulance.isLoading())
            ambulance.setUnload();
        else
            printError(sender, "This AmbulanceTeam is not loading an injured person now.");
        return;
    }

    private void printError(RealObject obj, String message) {
        if (PRINT_REASON_WHY_AGENT_COMMAND_WAS_NOT_EXECUTED)
            System.err.println("[Wrong Agent Command] time:" + WORLD.time() + ", agentID:" + obj.id + "\n  " + message);
    }
}
