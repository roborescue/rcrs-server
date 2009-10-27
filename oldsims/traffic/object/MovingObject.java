package traffic.object;
import java.io.*;
import java.util.*;
import traffic.*;
import rescuecore.OutputBuffer;
import rescuecore.RescueConstants;

public abstract class MovingObject extends RealObject implements Obstruction {
    public MovingObject(int id) {
        super(id);
    }

    private int m_position;
    private double m_positionExtra;
    private Route m_routePlan;

    public void setRoutePlan(Route route) {
        m_routePlan = route;
    }

    public Route routePlan() {
        return m_routePlan;
    }

    private int m_lastMovingTime = INITIALIZING_TIME;

    public void setLastMovingTime(int time) {
        m_lastMovingTime = time;
    }

    public int lastMovingTime() {
        return m_lastMovingTime;
    }

    private int m_lastLoadedOrUnloadedTime = INITIALIZING_TIME;

    public int x() {
        if (position() instanceof Edge) {
            Edge e = (Edge) position();
            return e.head().x()
                    + (int) ((e.tail().x() - e.head().x()) * m_positionExtra / e.length());
        }
        return position().x();
    }

    public int y() {
        if (position() instanceof Edge) {
            Edge e = (Edge) position();
            return e.head().y()
                    + (int) ((e.tail().y() - e.head().y()) * m_positionExtra / e.length());
        }
        return position().y();
    }

    public MotionlessObject motionlessPosition() {
        return position().motionlessPosition();
    }

    public RealObject position() {
        return (RealObject) WORLD.get(m_position);
    }

    public double positionExtra() {
        return m_positionExtra;
    }

    public void setPosition(int value) {
        if (!WORLD.isInitialized()) {
            m_position = value;
            copeWithBugOfGISInitilizer(m_positionExtra);
        }
    }

    public void setPositionExtra(int value) {
        if (!WORLD.isInitialized()) {
            m_positionExtra = value;
            copeWithBugOfGISInitilizer(value);
        }
        if (ASSERT) {
            boolean isValid = position() == null
                    || (position() instanceof Building || position() instanceof Node || position() instanceof AmbulanceTeam)
                    && m_positionExtra == 0 || position() instanceof Road && 0 <= m_positionExtra
                    && m_positionExtra <= ((Road) position()).length();
            Util.myassert(isValid, (isValid ? "" : "wrong positionExtra "
                    + m_positionExtra
                    + ", id:"
                    + id
                    + ", pos:"
                    + position()
                    + ", posID:"
                    + m_position
                    + ((position() instanceof Road)
                            ? ", length:" + ((Road) position()).length()
                            : "")));
        }
    }

    public void copeWithBugOfGISInitilizer(double posEx) {
        if (position() instanceof Building || position() instanceof Node
                || position() instanceof AmbulanceTeam) {
            if (posEx != 0)
                m_positionExtra = 0;
        }
        else if (position() instanceof Road) {
            Road rd = (Road) position();
            if (posEx < 0)
                m_positionExtra = 0;
            else if (posEx > rd.length())
                m_positionExtra = rd.length();
        }
    }

    public void input(String property, int[] value) {
        if ("POSITION".equals(property)) {
            setPosition(value[0]);
        }
        else if ("POSITION_EXTRA".equals(property)) {
            setPositionExtra(value[0]);
        }
        else {
            super.input(property, value);
        }
    }

    /** It's necessary to avoid existing on a blockade. */
    public void roundPositionExtra() {
        m_positionExtra = roundedPositionExtra();
    }

    private int roundedPositionExtra() {
        if (m_positionExtra == 0)
            return 0;
        if (ASSERT)
            Util.myassert(m_lane instanceof Lane);
        double movedLen = Math.min(movedLengthToForwardOfLane(), Math
                .ceil(m_lane.road.length() / 2) - 1);
        return (m_lane.forward == m_lane.road.head())
                ? m_lane.road.length() - (int) movedLen
                : (int) movedLen;
    }

    int m_submitedPos;
    int m_submitedPosEx;

    public boolean needsUpdate() {
        return !(lastMovingTime() < WORLD.time() - 1 && m_lastLoadedOrUnloadedTime != WORLD.time());
    }

    public void output(OutputBuffer out) {
		out.writeString(type());
		out.writeInt(id);
		OutputBuffer temp = new OutputBuffer();
		if (lastMovingTime() == WORLD.time() || m_lastLoadedOrUnloadedTime == WORLD.time()) {
			m_submitedPos = m_position;
			m_submitedPosEx = roundedPositionExtra();
			temp.writeString("POSITION");
			temp.writeInt(RescueConstants.INT_SIZE);
			temp.writeInt(m_submitedPos);
			temp.writeString("POSITION_EXTRA");
			temp.writeInt(RescueConstants.INT_SIZE);
			temp.writeInt(m_submitedPosEx);
		}
		if (lastMovingTime() == WORLD.time()) {
			// NOTE: keep structure of positionHistory of the Atsumi traffic simulator; it is a
			// list of node IDs.
			temp.writeString("POSITION_HISTORY");
			int[] history = new int[routePlan().size()];
			int count = 0;
			Iterator it = routePlan().iterator();
			while (it.hasNext()) {
				MotionlessObject obj = (MotionlessObject) it.next();
				if (obj instanceof Node) {
					if (ASSERT)
						Util.myassert(obj.id != 0,"wrong ID; it's impossible to divid ID and sentinel of ID list",obj.id);
					history[count++] = obj.id;
				}
				if (obj.id == m_submitedPos)
					break;
				if (ASSERT)
					Util.myassert(it.hasNext());
			}
			temp.writeInt((count + 1) * RescueConstants.INT_SIZE);
			temp.writeInt(count);
			for (int i=0;i<count;++i) temp.writeInt(history[i]);
		}
		else if (lastMovingTime() == WORLD.time() - 1) {
			temp.writeString("POSITION_HISTORY");
			temp.writeInt(RescueConstants.INT_SIZE);
			temp.writeInt(0);
		}
		temp.writeString("");
		byte[] bytes = temp.getBytes();
		out.writeInt(bytes.length);
		out.writeBytes(bytes);
	}

    private Lane m_lane;

    public Lane lane() {
        return m_lane;
    }

    public void setLane(Lane lane) {
        if (m_lane instanceof Lane)
            m_lane.removeMvObjOn(this);
        if (lane instanceof Lane)
            lane.addMvObjOn(this);
        m_lane = lane;
    }

    private int m_routeIndex;
    private int m_maxMarkingRouteIndex;
    private List m_movingLaneList = new ArrayList();
    private Building m_destBuilding;
    private Obstruction m_obstruction;

    public Obstruction obstruction() {
        return m_obstruction;
    }

    private void setObstruction(Obstruction obs) {
        if (m_obstruction instanceof MovingObject)
            ((MovingObject) m_obstruction).followingMvObjSet().remove(this);
        if (obs instanceof MovingObject)
            ((MovingObject) obs).followingMvObjSet().add(this);
        m_obstruction = obs;
    }

    private HashSet m_followingMvObjSet = new HashSet();

    public HashSet followingMvObjSet() {
        return m_followingMvObjSet;
    }

    private double m_velocity = 0;

    public double velocity() {
        return m_velocity;
    }

    private boolean m_didComplementRoute;
    private static int m_numReached;

    public static void resetNumReached() {
        m_numReached = 0;
    }

    public static int numReached() {
        return m_numReached;
    }

    public void incrementNumReached() {
        m_hasReachedDestination = true;
        m_numReached++;
    }

    private boolean m_hasReachedDestination;

    public boolean hasReachedDestination() {
        return m_hasReachedDestination;
    }

    public void initializeEveryCycle() {
        if (lastMovingTime() != WORLD.time()) {
            setRoutePlan(Route.singleObjRoute(motionlessPosition()));
        }
        else {
            if (position() instanceof AmbulanceTeam) {
                ((AmbulanceTeam) position()).unload();
                takeOffAmbulance();
            }
        }
        MotionlessObject dest = m_routePlan.get(m_routePlan.size() - 1);
        if (dest instanceof Building && m_routePlan.size() > 1) {
            m_destBuilding = (Building) dest;
            m_routePlan.remove();
        }
        else {
            m_destBuilding = null;
        }
        m_routeIndex = 0;
        m_routePlan.setIndexOfDestination();
        m_didComplementRoute = false;
        m_hasReachedDestination = false;
    }

    public void setMotionlessObstructionAndMovingLaneList() {
        m_movingLaneList.clear();
        MotionlessObject pos = (MotionlessObject) position();
        if (pos instanceof Building) {
            if (ASSERT)
                Util.myassert(m_velocity == 0, "wrong velocity in Building", m_velocity);
            m_maxMarkingRouteIndex = m_routeIndex;
            if (m_routeIndex == m_routePlan.size() - 1) {
                setObstruction(new Destination(pos, 0));
                return;
            }
            MotionlessObject prev = pos;
            MotionlessObject next = m_routePlan.get(m_routeIndex + 1);
            if (next instanceof Node) {
                // B -> N -> ...
                if (m_routeIndex + 1 == m_routePlan.size() - 1) {
                    // B -> N (or B -> N -> B)
                    setObstruction(new CuttingLanes(((Node) next).outLanes(), 0, next));
                    return;
                }
                // B -> N -> R -> ...
                prev = next;
                next = m_routePlan.get(m_routeIndex + 2);
            }
            // B -> N -> R -> ... or B -> R -> ...
            setObstruction(new CuttingLanes(((Road) next).lanesFrom((PointObject) prev), 0, next));
            return;
        }
        Lane lane = m_lane;
        m_movingLaneList.add(lane);
        if (ASSERT)
            Util.myassert(m_positionExtra != m_lane.road.length() / 2d || m_lane.isBlocked(),
                    "MvObj must not be on a blockade.");
        if (m_routeIndex == m_routePlan.indexOfDestination()) {
            if (pos instanceof Node) {
                m_maxMarkingRouteIndex = m_routeIndex;
                if (m_destBuilding != null && !this.canEnter(m_destBuilding)) {
                    setObstruction(Obstruction.BLOCKED_BUILDING);
                    System.out.println("set obstruction 1 for "+this.id);
                    return;
                }
                setObstruction(new Destination(pos, 0));
                return;
            }
            Road rd = (Road) pos;
            if (Math.floor(rd.length() / 2d - movedLengthToForwardOfLane()) <= 0) {
                m_maxMarkingRouteIndex = m_routeIndex;
                setObstruction(new Destination(pos, movedLengthToForwardOfLane()));
                return;
            }
        }
        for (int i = m_routeIndex; i < m_routePlan.size(); i++) {
            if (!m_didComplementRoute && i == m_routePlan.size() - 1)
                complementRoute(lane);
            MotionlessObject ml = m_routePlan.get(i);
            MotionlessObject next = (i + 1 < m_routePlan.size()) ? m_routePlan.get(i + 1) : null;
            if (ml instanceof Node) {
                Node nd = (Node) ml;
                if (i == m_routePlan.size() - 1) {
                    if (lane.adjacentLaneOfSamePriorityRoadVia(nd) == null) {
                        // DONOT: m_maxMarkingRouteIndex = i
                        setObstruction(new CuttingLanes(nd.outLanes(), 0, nd));
                        return;
                    }
                    if (m_destBuilding != null && !this.canEnter(m_destBuilding)) {
                        setObstruction(Obstruction.BLOCKED_BUILDING);
                        System.out.println("set obstruction 2 for "+this.id);
                        return;
                    }
                    setObstruction(Obstruction.DUMMY_OBSTRUCTION);
                    return;
                }
                if (next instanceof Road) {
                    Road rd = (Road) next;
                    if (nd == lane.back && rd != lane.road || nd == lane.forward && rd == lane.road) {
                        // -> -> N -> ...
                        // ... <- R <- <-
                        m_maxMarkingRouteIndex = i;
                        setObstruction(new CuttingLanes(rd.lanesFrom(nd), 0, nd));
                        return;
                    }
                    // ... -> N -> R -> ...
                    if (nd != pos && nd.shouldStop(lane, rd)) {
                        // DONOT: m_maxMarkingRouteIndex = i
                        setObstruction(new CuttingLanes(rd.lanesFrom(nd), 0, nd));
                        return;
                    }
                    else {
                        if (nd == lane.forward) {
                            List lanes = rd.lanesFrom(nd);
                            if (lane.priority > ((Lane) lanes.get(0)).priority) {
                                // DONOT: m_maxMarkingRouteIndex = i
                                setObstruction(new CuttingLanes(lanes, 0, nd));
                                return;
                            }
                            lane = (Lane) lanes.get(lane.nth);
                            m_movingLaneList.add(lane);
                        }
                        m_maxMarkingRouteIndex = i;
                        continue;
                    }
                }
                if (ASSERT)
                    Util.myassert(false,
                            "must not reach here; next of Node must be Road or Building.");
            }
            if (ml instanceof Road) {
                m_maxMarkingRouteIndex = i;
                Road rd = (Road) ml;
                if (i + 1 < m_routePlan.size() && next == lane.back) {
                    // -> R -> ...
                    // ... N <- <-
                    if (ASSERT)
                        Util.myassert(lane.road == rd);
                    double movedLengthToCurrentFwd = (lane == m_lane)
                            ? movedLengthToForwardOfLane()
                            : 0;
                    setObstruction(new TurningPoint(rd.lanesTo(lane.back), movedLengthToCurrentFwd,
                            rd));
                    return;
                }
                if (lane.isBlocked()
                        && (lane != m_lane || movedLengthToForwardOfLane() <= rd.length() / 2d)) {
                    setObstruction(new Blockade(lane));
                    return;
                }
                continue;
            }
            if (ASSERT)
                Util.myassert(false,
                        "must not reach here; route must consists of Road, Node or Building.");
        }
        setObstruction(Obstruction.DUMMY_OBSTRUCTION);
    }

    /** */
    private void complementRoute(Lane lane) {
        m_didComplementRoute = true;
        MotionlessObject destination = m_routePlan.destination();
        if (destination instanceof Road) {
            if (ASSERT)
                Util.myassert(destination == lane.road);
            m_routePlan.add(lane.forward);
        }
        else {
            if (ASSERT)
                Util.myassert(destination == lane.forward);
        }
        PointObject previousForward = lane.forward;
        Road origin = null;
        for (RoadCell roads = RoadCell.roadList(lane).forward(); roads != RoadCell.DUMMY_CELL
                && roads.road != origin; roads = roads.forward()) {
            Road rd = roads.road;
            if (origin == null)
                origin = rd;
            m_routePlan.add(rd);
            previousForward = (previousForward == rd.head()) ? rd.tail() : rd.head();
            m_routePlan.add(previousForward);
        }
        if (ASSERT) {
            int tmpPosId = m_position;
            m_position = m_routePlan.get(0).id;
            Util.myassert(m_routePlan.checkValidity(this), "wrong route complementation",
                    m_routePlan);
            m_position = tmpPosId;
        }
    }

    public void setMovingObstruction() {
        if ((m_obstruction instanceof Destination))
            return;
        MovingObject nearestObs = null;
        Iterator it = m_movingLaneList.iterator();
        if ((m_obstruction instanceof CuttingLanes || m_obstruction instanceof TurningPoint)
                && Math.floor(Math.abs(lengthToObstruction(m_obstruction)
                        - m_obstruction.minSafeDistance())) == 0) {
            if (it.hasNext()) {
                Lane ln = (Lane) it.next();
                if (ASSERT)
                    Util.myassert(ln == m_lane);
            }
        }
        boolean hasCheckedCurrentLane = false;
        while (it.hasNext()) {
            Lane ln = (Lane) it.next();
            double selfVal = (ln == m_lane && !hasCheckedCurrentLane)
                    ? movedLengthToForwardOfLane()
                    : -Double.MAX_VALUE;
            hasCheckedCurrentLane = true;
            double min = (!ln.isBlocked() || selfVal > ln.road.length() / 2d)
                    ? Double.MAX_VALUE
                    : ln.road.length() / 2d;
            // it2 gives all moving objects on the lane ln
            Iterator it2 = ln.mvObjOnSet().iterator();
            while (it2.hasNext()) {
                MovingObject mv = (MovingObject) it2.next();
                if (mv == this || mv instanceof Civilian && !(this instanceof Civilian))
                    continue;
                double val = mv.movedLengthToForwardOfLane();
                if (selfVal <= val && val <= min
                        && (val != selfVal || !(mv.obstruction() instanceof MovingObject))) {
                    min = val;
                    nearestObs = mv;
                }
            }
            if (nearestObs != null) {
                double remainLen = lengthToObstruction(nearestObs) - nearestObs.minSafeDistance();
                double tmpV = velocityWithFollowing();
                if (remainLen < 0 || remainLen < tmpV + brakingDistance(tmpV)) {
                    setObstruction(nearestObs);
                    return;
                }
                break;
            }
        }
        if (ASSERT)
            Util.myassert(m_obstruction.minSafeDistance() == 0,
                    "assume that minSafeDistance() of obstruction except MovingObject is zero.");
        MotionlessObject dest = m_routePlan.destination();
        Obstruction destObs = new Destination(dest, (dest instanceof Road)
                ? ((Road) dest).length() / 2d
                : 0);
        int destIndex = m_routePlan.indexOfDestination();
        if (m_maxMarkingRouteIndex >= destIndex
                && lengthToObstruction(m_obstruction) > lengthToObstruction(destObs)) {
            m_maxMarkingRouteIndex = destIndex;
            setObstruction(destObs);
        }
    }

    private boolean m_moved;

    public boolean moved() {
        return m_moved;
    }

    public void setMoved(boolean moved) {
        m_moved = moved;
    }

    public void move() {
        _move();
        setMoved(true);
    }

    private void _move() {
        if (ASSERT)
            Util.myassert(m_routeIndex <= m_routePlan.indexOfDestination(),
                    "MvObj moved over destination", m_routeIndex);
        if (m_moved)
            return;
        if (m_hasReachedDestination)
            return;
        setDestinationObstruction();
        if (Math.floor(m_velocity) == 0
                && Math.floor(lengthToObstruction(m_obstruction)) == m_obstruction
                        .minSafeDistance()) {
            m_velocity = 0;
            if (m_obstruction instanceof Destination)
                stop();
            else if (m_obstruction == Obstruction.BLOCKED_BUILDING)
                return;
            else if (m_obstruction instanceof Blockade)
                avoidBlockade();
            else if (m_obstruction instanceof CuttingLanes)
                cutIntoLanes();
            else if (m_obstruction instanceof TurningPoint)
                turn();
            else if (m_obstruction instanceof MovingObject)
                overtake();
            else if (ASSERT)
                Util.myassert(false, "must not reach here; "
                        + "m_obstruction must not be null or DUMMY_OBSTRUCTION when do not run");
            return;
        }
        moveWithFollowing();
    }

    private void cutIntoLanes() {
        cutIntoLanes(false);
    }

    private void cutIntoLanes(boolean dontCutWhenCantProgress) {
        // Bad implementation; too many member variable.
        Lane formerLane = m_lane;
        int formerPosition = m_position;
        double formerPositionExtra = m_positionExtra;
        int formerRouteIndex = m_routeIndex;
        int formerMaxMarkingRouteIndex = m_maxMarkingRouteIndex;
        Obstruction formerObstruction = m_obstruction;
        List formerMovingLaneList = m_movingLaneList;
        m_movingLaneList = new ArrayList();
        Obstruction cuttingPoint = m_obstruction;
        Iterator it = ((CuttingLanes) m_obstruction).lanes.iterator();
        while (it.hasNext()) {
            m_maxMarkingRouteIndex = formerMaxMarkingRouteIndex;
            setObstruction(formerObstruction);
            Lane ln = (Lane) it.next();
            putIntoNewLane(ln);
            setMotionlessObstructionAndMovingLaneList();
            setMovingObstruction();
            double len = lengthToObstruction(m_obstruction);
            double msd = m_obstruction.minSafeDistance();
            if (!(OVERLOOK_EXCESSIVE_CHANGE_LANES && WORLD.sec() == 0)) {
                if (len < msd || dontCutWhenCantProgress && Math.floor(len - msd) == 0
                        || isComingMovObjFromBackLanes(ln, cuttingPoint))
                    continue;
            }
            moveWithFollowing();
            return;
        }
        setLane(formerLane);
        m_position = formerPosition;
        m_positionExtra = formerPositionExtra;
        m_routeIndex = formerRouteIndex;
        m_maxMarkingRouteIndex = formerMaxMarkingRouteIndex;
        setObstruction(formerObstruction);
        m_movingLaneList = formerMovingLaneList;
    }

    private void putIntoNewLane(Lane lane) {
        setLane(lane);
        m_routeIndex = m_routePlan.indexOf(((CuttingLanes) m_obstruction).motionlessPosition(),
                m_routeIndex);
        if (ASSERT)
            Util.myassert(m_routeIndex != -1);
        m_position = m_routePlan.get(m_routeIndex).id;
        double len = ((CuttingLanes) m_obstruction).lengthToForwardOfLane();
        m_positionExtra = (position() instanceof Road) ? (lane.forward == lane.road.head())
                ? lane.road.length() - len
                : len : 0;
    }

    private boolean isComingMovObjFromBackLanes(Lane lane, Obstruction cuttingPoint) {
        if (ASSERT)
            Util.myassert(MAX_MAX_VELOCITY >= maxVelocity(), "Update MAX_MAX_VELOCITY to",
                    maxVelocity());
        PointObject preBack = lane.forward;
        MovingObject nearestMv = null;
        Road origin = null;
        for (RoadCell roads = RoadCell.roadList(lane); roads != RoadCell.DUMMY_CELL; roads = roads
                .back()) {
            lane = (Lane) roads.road.lanesTo(preBack).get(lane.nth);
            double selfVal = (lane == m_lane && origin == null)
                    ? movedLengthToForwardOfLane()
                    : Double.MAX_VALUE;
            double max = (!lane.isBlocked() || selfVal <= roads.road.length() / 2d)
                    ? -Double.MAX_VALUE
                    : roads.road.length() / 2;
            Iterator it = lane.mvObjOnSet().iterator();
            while (it.hasNext()) {
                MovingObject mv = (MovingObject) it.next();
                if (mv == this || mv instanceof Civilian && !(this instanceof Civilian))
                    continue;
                double val = mv.movedLengthToForwardOfLane();
                if (max < val && val <= selfVal) {
                    max = val;
                    nearestMv = mv;
                }
            }
            if (nearestMv != null) {
                if (!nearestMv.hasPlanTo(cuttingPoint.motionlessPosition()))
                    return false;
                double remainLen = nearestMv.lengthToObstruction(cuttingPoint) - minSafeDistance();
                if (remainLen < 0)
                    return true;
                double tmpV = (nearestMv.moved()) ? nearestMv.velocity() : nearestMv
                        .velocityWithFollowing();
                return (remainLen < tmpV + brakingDistance(tmpV));
            }
            if (lane.isBlocked() && selfVal > roads.road.length() / 2d)
                return false;
            if (roads.road == origin)
                break;
            if (origin == null)
                origin = roads.road;
            preBack = lane.back;
        }
        return false;
    }

    public boolean hasPlanTo(MotionlessObject to) {
        return m_routePlan.indexOf(to, m_routeIndex) >= 0;
    }

    private void turn() {
        TurningPoint tp = (TurningPoint) m_obstruction;
        double len = ((Road) tp.motionlessPosition()).length() - tp.lengthToForwardOfLane();
        setObstruction(new CuttingLanes(tp.lanes, len, tp.motionlessPosition()));
        cutIntoLanes();
    }

    private void overtake() {
        MovingObject fwdMv = (MovingObject) m_obstruction;
        if (m_lane.priority == 1 && fwdMv.hasReachedDestination()) {
            incrementNumReached();
            return;
        }
        setObstruction(new CuttingLanes(m_lane.road.lanesTo(m_lane.forward),
                movedLengthToForwardOfLane(), m_lane.road));
        cutIntoLanes(true);
    }

    private void avoidBlockade() {
        if (m_position == m_routePlan.destination().id)
            return;
        Road road = m_lane.road;
        int numAlive = road.aliveLinesTo(m_lane.forward);
        int numBlocked = road.blockedLines();
        if (numAlive == 0)
            return;
        ListIterator lit = road.lanesTo(m_lane.forward).listIterator(numBlocked);
        List list = new ArrayList(numAlive);
        while (lit.hasNext()) {
            Lane ln = (Lane) lit.next();
            if (ASSERT)
                Util.myassert(!ln.isBlocked());
            list.add(ln);
        }
        if (ASSERT)
            Util.myassert(list.size() == numAlive);
        list.remove(m_lane);
        setObstruction(new CuttingLanes(list, movedLengthToForwardOfLane(), road));
        cutIntoLanes(true);
    }

    private void stop() {
        if (ASSERT)
            Util.myassert(!m_hasReachedDestination,
                    "must not reach here; agent has already reached destination.");
        if (position() instanceof Building) {
            incrementNumReached();
        }
        else if (m_destBuilding instanceof Building) {
            if (this.canEnter(m_destBuilding))
                enter();
            else
                stopWithoutEntering();
        }
        else if (m_lane.nth == 0) {
            incrementNumReached();
        }
        else {
            setObstruction(new CuttingLanes((Lane) m_lane.road.lanesTo(m_lane.forward).get(
                    m_lane.nth - 1), movedLengthToForwardOfLane(), motionlessPosition()));
            cutIntoLanes();
        }
    }

    private boolean canEnter(Building b) {
        if ((this instanceof FireBrigade) && b.cannotBeEnteredByFB())
            return false;
        return true;
    }
    
    private void enter() {
        m_position = m_destBuilding.id;
        m_positionExtra = 0;
        m_routeIndex = m_routePlan.indexOfDestination();
        m_routePlan.subList(m_routeIndex + 1, m_routePlan.size()).clear();
        if (ASSERT)
            Util.myassert(checkRouteConnection());
        m_routePlan.add(m_destBuilding);
        setLane(null);
        incrementNumReached();
    }

    private void stopWithoutEntering() {
        // not sure about this one, but hopefully prevents further computation in this cycle
        incrementNumReached();
    }

    private boolean checkRouteConnection() {
        if (m_routePlan.isEmpty())
            return true;
        Iterator it = m_routePlan.iterator();
        MotionlessObject prev = (MotionlessObject) it.next();
        while (it.hasNext()) {
            MotionlessObject ml = (MotionlessObject) it.next();
            if (!prev.isAdjacentTo(ml))
                return false;
            prev = ml;
        }
        return true;
    }

    private void moveWithFollowing() {
        m_velocity = velocityWithFollowing(true);
        move(m_velocity);
    }

    private void setDestinationObstruction() {
        if (!(m_obstruction instanceof MovingObject))
            return;
        int destIndex = m_routePlan.indexOfDestination();
        int mvObsIndex = m_routePlan.indexOf((MotionlessObject) ((MovingObject) m_obstruction)
                .position(), m_routeIndex);
        if (destIndex > mvObsIndex)
            return;
        MotionlessObject dest = m_routePlan.destination();
        Obstruction destObs = new Destination(dest, (dest instanceof Road)
                ? ((Road) dest).length() / 2d
                : 0);
        if (lengthToObstruction(m_obstruction) - lengthToObstruction(destObs) <= m_obstruction
                .minSafeDistance())
            return;
        setObstruction(destObs);
        m_maxMarkingRouteIndex = destIndex;
    }

    public double velocityWithFollowing() {
        return velocityWithFollowing(false);
    }

    private double velocityWithFollowing(boolean doChange) {
        try {
            return _velocityWithFollowing(doChange);
        }
        catch (Error e) {
            // Platoon can ignore Civilian, so Civilian comes face to face with this exception
            // often.
            if (this instanceof Civilian)
                return 0;
            throw (Error) e.fillInStackTrace();
        }
    }

    /**
     * CAUTION: Bad implementation; this method chages m_obstruction and m_maxMarkingRouteIndex if
     * it's necessary to do so.
     */
    private double _velocityWithFollowing(boolean doChange) {
        Obstruction formerObstruction = m_obstruction;
        int formerMaxMarkingRouteIndex = m_maxMarkingRouteIndex;
        setDestinationObstruction();
        Obstruction obstruction = m_obstruction;
        if (!doChange) {
            setObstruction(formerObstruction);
            m_maxMarkingRouteIndex = formerMaxMarkingRouteIndex;
        }
        double msd = obstruction.minSafeDistance();
        double dx = lengthToObstruction(obstruction);
        // MvObj can stop urgently only at first second each cycle
        if ((WORLD.sec() == 0 || (OVERLOOK_EXCESSIVE_CHANGE_LANES && WORLD.sec() == 1))
                && (dx - msd <= Math.max(0, m_velocity - maxAcceleration() * 2d) || accelleration(
                        dx, msd) < -maxAcceleration() * 2d))
            return 0;
        if (dx < msd) {
            if (ASSERT)
                Util.myassert(m_velocity == 0 || Math.floor(msd - dx) == 0, // in
                                                                                                                        // order
                                                                                                                        // to
                                                                                                                        // reach
                                                                                                                        // Node
                                                                                                                        // destination
                                                                                                                        // (cf.
                                                                                                                        // "void
                                                                                                                        // move(double
                                                                                                                        // length)")
                        "wrong distance (dx - msd) or velocity");
            return 0;
        }
        double a = accelleration(dx, msd);
        if (ASSERT)
            Util.myassert(a >= -maxAcceleration() * 2d, "wrong accelleration;  -"
                    + maxAcceleration() * 2d + " > ", a);
        double result = m_velocity + Math.min(a, maxAcceleration());
        if (result < 0 && Math.ceil(result) == 0)
            result = 0;
        if (ASSERT)
            Util.myassert(result >= 0, "wrong velocity", result);
        return Math.min(result, maxVelocity());
    }

    private double accelleration(double dx, double msd) {
        return maxAcceleration() * (Math.sqrt(1 + 2 * (dx - msd) / maxAcceleration()) - 1)
                - m_velocity;
    }

    public double lengthToObstruction(Obstruction obstruction) {
        if (obstruction == Obstruction.DUMMY_OBSTRUCTION)
            return Double.POSITIVE_INFINITY;
        MotionlessObject obsPos = obstruction.motionlessPosition();
        double length = (m_lane instanceof Lane) ? -movedLengthToForwardOfLane() : 0;
        boolean hasReachedCurrentLane = false;
        double lenIfNotLoot = Double.POSITIVE_INFINITY;
        Iterator it = m_movingLaneList.iterator();
        while (it.hasNext()) {
            Lane ln = (Lane) it.next();
            if (ln.back == obsPos || ln.road == obsPos)
                if (hasReachedCurrentLane || ln != m_lane
                        || obstruction.lengthToForwardOfLane() >= movedLengthToForwardOfLane()) // for
                                                                                                                                                                        // a
                                                                                                                                                                        // loop
                    break;
                else
                    lenIfNotLoot = length;
            if (!hasReachedCurrentLane)
                if (ln == m_lane)
                    hasReachedCurrentLane = true;
                else
                    continue;
            length += ln.road.length();
            if (!it.hasNext()) {
                if (ASSERT)
                    Util.myassert(hasReachedCurrentLane,
                            "the current lane is not on the moving lane list.");
                if (ln.forward == obsPos)
                    break;
                if (lenIfNotLoot != Double.POSITIVE_INFINITY) {
                    length = lenIfNotLoot;
                    break;
                }
                // TODO: remake this method.
                // This is a first aid.
                length = (m_lane instanceof Lane && position() instanceof Road)
                        ? -movedLengthToForwardOfLane()
                        : 0;
                int i;
                for (i = m_routeIndex; i < m_routePlan.size(); i++) {
                    MotionlessObject ml = m_routePlan.get(i);
                    if (ml == obsPos)
                        break;
                    if (ml instanceof Road)
                        length += ((Road) ml).length();
                }
                if (ASSERT)
                    Util.myassert(i < m_routePlan.size(), "obstruction is not on route plan",
                            obstruction.motionlessPosition());
                if (i >= m_routePlan.size())
                    return 0; // This line is just to make sure.
                break;
            }
        }
        if (obsPos instanceof Road)
            length += obstruction.lengthToForwardOfLane();
        return length;
    }

    private void move(double length) {
        if (ASSERT)
            Util.myassert(length >= 0);
        if (m_lane instanceof Lane)
            length += movedLengthToForwardOfLane();
        length -= 0.001; // It's necessary to avoid reaching a blockade.
        Iterator it = m_movingLaneList.iterator();
        while (it.hasNext() && length > 0) {
            Lane ln = (Lane) it.next();
            m_routeIndex = m_routePlan.indexOf(ln, m_routeIndex, m_maxMarkingRouteIndex + 1);
            if (ASSERT)
                Util.myassert(m_routeIndex != -1, "lane is not on moving lane list", ln);
            double movingLength;
            if (Math.ceil(length) >= ln.road.length()) { // in order
                                                                                        // to reach
                                                                                        // Node
                                                                                        // destination.
                                                                                        // (see also
                                                                                        // "double
                                                                                        // _velocityWithFollowing(boolean
                                                                                        // doChange)")
                movingLength = Math.min(ln.road.length(), length);
            }
            else {
                movingLength = length;
                if (m_routePlan.get(m_routeIndex) == ln.forward)
                    m_routeIndex--;
            }
            length -= movingLength;
            m_position = m_routePlan.get(m_routeIndex).id;
            m_positionExtra = (position() instanceof Road) ? positionExtra(ln, movingLength) : 0;
            setLane(ln);
        }
        if (ASSERT)
            Util.myassert(Math.floor(Math.abs(length)) == 0, "wrong length", length);
    }

    public double movedLengthToForwardOfLane() {
        if (ASSERT)
            Util.myassert(!(position() instanceof Building));
        if (m_position == m_lane.road.id)
            return (m_lane.forward == m_lane.road.head())
                    ? m_lane.road.length() - m_positionExtra
                    : m_positionExtra;
        if (ASSERT)
            Util.myassert(position() instanceof Node);
        if (ASSERT)
            Util.myassert(m_position == m_lane.forward.id || m_position == m_lane.back.id);
        return (m_position == m_lane.forward.id) ? m_lane.road.length() : 0;
    }

    private double positionExtra(Lane lane, double movingLength) {
        return (lane.forward == lane.road.head())
                ? lane.road.length() - movingLength
                : movingLength;
    }

    private double brakingDistance(double velocity) {
        return velocity / 2d * Math.ceil(velocity / maxAcceleration());
    }

    public void takeOnAmbulance(AmbulanceTeam ambulance) {
        m_position = ambulance.id;
        m_positionExtra = 0;
        m_velocity = 0;
        setLane(null);
        m_routeIndex = m_routePlan.indexOfDestination();
        m_lastLoadedOrUnloadedTime = WORLD.time();
    }

    public void takeOffAmbulance() {
        AmbulanceTeam ambulance = (AmbulanceTeam) position();
        m_position = ambulance.position().id;
        m_positionExtra = ambulance.positionExtra();
        m_velocity = 0;
        setLane(ambulance.lane());
        m_routeIndex = m_routePlan.indexOfDestination();
        m_lastLoadedOrUnloadedTime = WORLD.time();
    }

    public double maxAcceleration() {
        return MAX_ACCELERATION;
    }

    public double maxVelocity() {
        return MAX_VELOCITY;
    }

    // ------------------------------------------------------ Obstruction
    public double minSafeDistance() {
        return MIN_SAFE_DISTANCE_BETWEEN_CARS;
    }

    public double lengthToForwardOfLane() {
        return movedLengthToForwardOfLane();
    }

    private void dp() {
        dp("");
    }

    private void dp(String str) {
        ndp("sec:" + WORLD.sec() + ", id:" + id + str + "\n");
    }

    private void ndp(String str) {
        if (true)
            System.out.print(str);
    }

public String stringOfPlan() {
    StringBuffer result = new StringBuffer().append("{");
    for (int i = 0; i < m_routePlan.size(); i++) {
        if (i == m_routeIndex)
            result.append("<< ");
        if (i == m_routePlan.indexOfDestination())
            result.append("[");
        MotionlessObject m = m_routePlan.get(i);
        if (m instanceof Building)
            result.append("B").append(m.id);
        else if (m instanceof Node)
            result.append("N").append(m.id);
        else if (m instanceof Road)
            result.append("R(").append(((Road) m).head().id).append(")").append(m.id).append(
                                                                                             "(").append(((Road) m).tail().id).append(")");
        if (i == m_routePlan.indexOfDestination())
            result.append("]");
        if (i == m_maxMarkingRouteIndex)
            result.append(" >>");
        if (i < m_routePlan.size() - 1)
            result.append(", ");
    }
    return result.append("}").toString();
}
}
