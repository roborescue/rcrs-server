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
            String type = in.readString();
            if ("".equals(type)) return;
            int id = in.readInt();
            int size = in.readInt();
            RescueObject obj = get(id);
            if (obj == null) {
                obj = newRescueObject(type, id);
                if (obj instanceof RealObject)
                    add((RealObject) obj);
            }
            String prop = null;
            while (!"".equals(prop)) {
                prop = in.readString();
                if (!"".equals(prop)) {
                    setProperty(prop, in, obj);
                }
            }
        }
    }

    private RescueObject newRescueObject(String type, int id) {
        if ("WORLD".equals(type)) {
            return new World(id);
        }
        else if ("RIVER".equals(type)) {
            return new River(id);
        }
        else if ("RIVER_NODE".equals(type)) {
            return new RiverNode(id);
        }
        else if ("ROAD".equals(type)) {
            return new Road(id);
        }
        else if ("NODE".equals(type)) {
            return new Node(id);
        }
        else if ("BUILDING".equals(type)) {
            return new Building(id);
        }
        else if ("AMBULANCE_CENTRE".equals(type)) {
            return new AmbulanceCenter(id);
        }
        else if ("FIRE_STATION".equals(type)) {
            return new FireStation(id);
        }
        else if ("POLICE_OFFICE".equals(type)) {
            return new PoliceOffice(id);
        }
        else if ("REFUGE".equals(type)) {
            return new Refuge(id);
        }
        else if ("CIVILIAN".equals(type)) {
            return new Civilian(id);
        }
        else if ("AMBULANCE_TEAM".equals(type)) {
            return new AmbulanceTeam(id);
        }
        else if ("FIRE_BRIGADE".equals(type)) {
            return new FireBrigade(id);
        }
        else if ("POLICE_FORCE".equals(type)) {
            return new PoliceForce(id);
        }
        else if ("CAR".equals(type)) {
            return new Car(id);
        }
        else {
            if (ASSERT) {
                Util.myassert(false, "illeagle object type", type);
            }
            return null;
        }
    }

    private void setProperty(String property, InputBuffer data, RescueObject obj) {
        int size = data.readInt();
        int[] val = null;
        if ("EDGES".equals(property)
            || "SIGNAL_TIMING".equals(property)
            || "SHORTCUT_TO_TURN".equals(property)
            || "POCKET_TO_TURN_ACROSS".equals(property)
            || "POSITION_HISTORY".equals(property)
            || "ENTRANCES".equals(property)
            || "BUILDING_APEXES".equals(property)) {
            val = new int[data.readInt()];
            for (int i=0;i<val.length;++i) val[i] = data.readInt();
        }
        else {
            val = new int[] {data.readInt()};
        }
        if (obj != null) {
            obj.input(property, val);
        }
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
            String command = in.readString();
            int size = in.readInt();
            if ("AK_MOVE".equals(command)) {
                parseAK_MOVE(in);
            }
            else if ("AK_LOAD".equals(command)) {
                parseAK_LOAD(in);
            }
            else if ("AK_UNLOAD".equals(command)) {
                parseAK_UNLOAD(in);
            }
            else {
                in.skip(size);
            }
        }
    }

    private void parseAK_MOVE(InputBuffer in) {
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
        agent.setRoutePlan(submitedRoute.checkValidity(agent) ? submitedRoute : Route.singleObjRoute(agent.motionlessPosition()));
    }

    private void parseAK_LOAD(InputBuffer in) {
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
                                                         && ((Node) ambulance.position()).edges().contains(mv.position()) || ambulance.position() instanceof Road
                                                         && (((Road) ambulance.position()).head() == mv.position() || ((Road) ambulance.position()).tail() == mv.position())))) {
            ambulance.setLoad(mv);
        }
        else {
            printError(sender, "Wrong position: This AmbulanceTeam's position ("
                       + ambulance.position().id + ") is not the same position of the target's ("
                       + mv.position().id + ").");
        }
        return;
    }

    private void parseAK_UNLOAD(InputBuffer in) {
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
