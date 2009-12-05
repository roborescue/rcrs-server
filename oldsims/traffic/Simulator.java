package traffic;
import java.net.*;
import java.util.*;
import traffic.object.*;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class Simulator implements Constants {
    private static final Log LOG = LogFactory.getLog(Simulator.class);

    private int id;

    public Simulator() {
    }

    public void setInitialPosition() {
        for (int i = WORLD.movingObjectArray().length - 1; i >= 0; i--) {
            MovingObject mv = WORLD.movingObjectArray()[i];
            Lane lane;
            MotionlessObject mlpos = mv.motionlessPosition();
            if (mlpos instanceof Building) {
                lane = null;
            }
            else if (mlpos instanceof Node) {
                List ols = ((Node) mlpos).outLanes();
                lane = initialPosition(ols, 0);
                lane = (lane instanceof Lane) ? lane : (Lane) Util.getAtRandom(ols);
            }
            else {
                if (ASSERT)
                    Util.myassert(mlpos instanceof Road, "unexpected motionlessPosition", mlpos);
                List lsh = ((Road) mlpos).lanesToHead();
                List lst = ((Road) mlpos).lanesToTail();
                Lane lh = initialPosition(lsh, mv.positionExtra());
                Lane lt = initialPosition(lst, mv.positionExtra());
                boolean hOrT = RANDOM.nextDouble() < lsh.size() / (lsh.size() + lst.size());
                if (lh instanceof Lane && (lt == null || hOrT))
                    lane = lh;
                else if (lt instanceof Lane)
                    lane = lt;
                else
                    lane = (Lane) Util.getAtRandom((hOrT) ? lsh : lst);
            }
            mv.setLane(lane);
            mv.roundPositionExtra();
        }
    }

    private Lane initialPosition(List candidates, double positionExtra) {
        Iterator it = candidates.iterator();
        while (it.hasNext()) {
            Lane lane = (Lane) it.next();
            if (!areThereOthers(lane, positionExtra))
                return lane;
        }
        return null;
    }

    private boolean areThereOthers(Lane lane, double positionExtra) {
        Iterator it = lane.mvObjOnSet().iterator();
        while (it.hasNext()) {
            MovingObject mv = (MovingObject) it.next();
            if (mv.positionExtra() == positionExtra)
                return true;
        }
        return false;
    }

    public void step() {
        LOG.info("time: " + WORLD.time());
        move();
        loadUnload();
    }

    private void move() {
        MovingObject[] mvObjs = WORLD.movingObjectArray();
        for (int i = mvObjs.length - 1; i >= 0; i--)
            mvObjs[i].initializeEveryCycle();
        MovingObject.resetNumReached();
        long startTime = System.currentTimeMillis();
        WORLD.shuffleMvObjsArray();
        for (WORLD.setSec(0); WORLD.sec() < 60; WORLD.progressSec()) {
            if (MovingObject.numReached() == mvObjs.length
                    || System.currentTimeMillis() - startTime > CALCULATING_LIMIT_MILLI_SEC)
                break;
			//            if (USE_VIEWER)
				//                VIEWER.waitProgress();
			//            if (USE_VIEWER)
			//                VIEWER.setTime();
            for (int i = mvObjs.length - 1; i >= 0; i--) {
                MovingObject mv = mvObjs[i];
                if (!(mv.position() instanceof AmbulanceTeam) && !mv.hasReachedDestination()) {
                    mv.setMoved(false);
                    mv.setMotionlessObstructionAndMovingLaneList();
                }
            }
            for (int i = mvObjs.length - 1; i >= 0; i--) {
                MovingObject mv = mvObjs[i];
                if (!(mv.position() instanceof AmbulanceTeam) && !mv.hasReachedDestination())
                    mv.setMovingObstruction();
            }
            sortByObstruction();
            Iterator it;
            Collections.shuffle(m_waitingNoChangeList, RANDOM);
            it = m_waitingNoChangeList.iterator();
            while (it.hasNext()) {
                m_loopOrigin = (MovingObject) it.next();
                moveBeforeForwardMvObj(m_loopOrigin);
            }
            m_waitingNoChangeList.clear();
            Collections.shuffle(m_waitingList, RANDOM);
            it = m_waitingList.iterator();
            while (it.hasNext())
                dispatch((ArrayList) it.next());
            m_waitingList.clear();
            Collections.shuffle(m_noWaitingList, RANDOM);
            it = m_noWaitingList.iterator();
            while (it.hasNext())
                ((MovingObject) it.next()).move();
            m_noWaitingList.clear();
            it = m_noWaitingNoChageList.iterator();
            while (it.hasNext())
                ((MovingObject) it.next()).move();
            m_noWaitingNoChageList.clear();
			//            if (USE_VIEWER)
			//                VIEWER.repaint();
        }
    }

    private ArrayList m_noWaitingNoChageList = new ArrayList();
    private ArrayList m_noWaitingList = new ArrayList();
    private HashMap m_waitingMap = new HashMap();
    private ArrayList m_waitingList = new ArrayList();
    private ArrayList m_waitingNoChangeList = new ArrayList();
    private MovingObject m_loopOrigin;

    private void sortByObstruction() {
        for (int i = WORLD.movingObjectArray().length - 1; i >= 0; i--) {
            MovingObject mv = WORLD.movingObjectArray()[i];
            if (mv.hasReachedDestination())
                continue;
            if (mv.position() instanceof AmbulanceTeam) {
                mv.incrementNumReached();
                continue;
            }
            if (mv.obstruction() == Obstruction.DUMMY_OBSTRUCTION) {
                m_noWaitingNoChageList.add(mv);
            }
            else if (mv.obstruction() instanceof Destination) {
                m_noWaitingList.add(mv);
            }
            else if (mv.obstruction() instanceof Blockade
                    || mv.obstruction() instanceof CuttingLanes
                    || mv.obstruction() instanceof TurningPoint) {
                ArrayList follows = (ArrayList) m_waitingMap.get(mv.obstruction()
                        .motionlessPosition());
                if (follows == null) {
                    follows = new ArrayList();
                    m_waitingMap.put(mv.obstruction().motionlessPosition(), follows);
                    m_waitingList.add(follows);
                }
                follows.add(mv);
            }
            else if (mv.obstruction() instanceof MovingObject) {
                m_waitingNoChangeList.add(mv);
            }
            else if (mv.obstruction() == Obstruction.BLOCKED_BUILDING) {
                m_waitingNoChangeList.add(mv);                
            }
            else {
                if (ASSERT)
                    Util.myassert(false);
            }
            if (ASSERT)
                Util.myassert(mv.obstruction().minSafeDistance() <= MAX_MIN_SAFE_DISTANCE,
                        "Update MAX_MIN_SAFE_DISTANCE to", mv.obstruction().minSafeDistance());
        }
        m_waitingMap.clear();
    }

    private void moveBeforeForwardMvObj(MovingObject mv) {
        if (mv.moved())
            return;
        MovingObject[] children = (MovingObject[]) mv.followingMvObjSet().toArray(
                new MovingObject[0]);
        for (int i = 0; i < children.length; i++)
            if (children[i] != m_loopOrigin)
                moveBeforeForwardMvObj(children[i]);
        if (ASSERT)
            Util.myassert(!mv.moved());
        mv.move();
    }

    private void dispatch(ArrayList follows) {
        Collections.shuffle(follows, RANDOM);
        boolean hasAnyonePassed = false;
        Iterator it = follows.iterator();
        while (it.hasNext()) {
            MovingObject mv = (MovingObject) it.next();
            Obstruction obs = mv.obstruction();
            if (!hasAnyonePassed || Math.floor(mv.lengthToObstruction(obs)) > obs.minSafeDistance()
                    || Math.floor(mv.velocity()) != 0) { // if the mv needs
                                                                                // to stop urgently,
                                                                                // then do it.
                if (!mv.moved()) {
                    mv.move();
                    if (mv.obstruction() != obs)
                        if (!hasAnyonePassed)
                            hasAnyonePassed = !(OVERLOOK_EXCESSIVE_CHANGE_LANES && WORLD.sec() == 0);
                        else if (ASSERT)
                            Util
                                    .myassert(false,
                                            "Two or more agent must not pass one MotionlessObject in a second.");
                }
            }
            else {
                mv.setMoved(true);
            }
        }
    }

    private void loadUnload() {
        Collections.shuffle(WORLD.ambulanceTeamList, RANDOM);
        Iterator it = WORLD.ambulanceTeamList.iterator();
        while (it.hasNext()) {
            AmbulanceTeam ambulance = (AmbulanceTeam) it.next();
            MovingObject target = ambulance.loadingMvObj();
            if (target == null)
                continue;
            if (ambulance.doLoad()) {
                if (target.lastMovingTime() == WORLD.time()) {
                    ambulance.resetLoad();
                    printReason(ambulance, "The target agent(ID:" + target.id + ") was moving.");
                }
                else if (target instanceof AmbulanceTeam && ((AmbulanceTeam) target).isLoading()) {
                    ambulance.resetLoad();
                    printReason(ambulance, "The target agent(ID:" + target.id + ") is/was loading.");
                }
                else {
                    if (target.position() instanceof MotionlessObject) {
                        target.takeOnAmbulance(ambulance);
                        ambulance.load();
                    }
                    else {
                        ambulance.resetLoad();
                        printReason(ambulance, "The target agent(ID:" + target.id
                                + ") had already been on another MovingObject.");
                    }
                }
            }
            else if (ambulance.doUnload()) {
                if (ASSERT)
                    Util.myassert(target.position() == ambulance,
                            "Wrong Command; AmbulanceTeam(ID:" + ambulance.id
                                    + ") is not loading a target.");
                target.takeOffAmbulance();
                ambulance.unload();
            }
        }
    }

    private void printReason(MovingObject mv, String reason) {
        if (PRINT_REASON_WHY_AGENT_COMMAND_WAS_NOT_EXECUTED)
            LOG.debug("[AK_LOAD was ignored] time:" + WORLD.time() + ", ambulanceID:"
                    + mv.id + "\n  " + reason);
    }
}
