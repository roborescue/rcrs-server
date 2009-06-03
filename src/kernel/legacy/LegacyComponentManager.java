package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;

import kernel.Kernel;
import kernel.Viewer;
import kernel.DefaultViewer;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.VKConnect;
import rescuecore2.messages.control.VKAcknowledge;
import rescuecore2.messages.control.KVConnectOK;
import rescuecore2.messages.control.SKConnect;
import rescuecore2.messages.control.SKAcknowledge;
import rescuecore2.messages.control.KSConnectOK;
import rescuecore2.worldmodel.Property;

import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.entities.Civilian;
import rescuecore2.version0.entities.FireBrigade;
import rescuecore2.version0.entities.FireStation;
import rescuecore2.version0.entities.AmbulanceTeam;
import rescuecore2.version0.entities.AmbulanceCentre;
import rescuecore2.version0.entities.PoliceForce;
import rescuecore2.version0.entities.PoliceOffice;
import rescuecore2.version0.entities.Road;
import rescuecore2.version0.entities.Node;
import rescuecore2.version0.entities.Building;
import rescuecore2.version0.entities.RescuePropertyType;
import rescuecore2.version0.messages.AKConnect;
import rescuecore2.version0.messages.AKAcknowledge;
import rescuecore2.version0.messages.KAConnectError;
import rescuecore2.version0.messages.KAConnectOK;

/**
   Class that manages connecting legacy components.
 */
public class LegacyComponentManager implements ConnectionManagerListener {
    private static final int WAIT_TIME = 10000;

    private Kernel kernel;

    // Entities that have no controller yet
    private Queue<Civilian> civ;
    private Queue<FireBrigade> fb;
    private Queue<FireStation> fs;
    private Queue<AmbulanceTeam> at;
    private Queue<AmbulanceCentre> ac;
    private Queue<PoliceForce> pf;
    private Queue<PoliceOffice> po;

    // Connected agents
    private Set<LegacyAgent> agentsToAcknowledge;

    // Connected simulators
    private Set<LegacySimulator> simsToAcknowledge;
    private int nextSimulatorID;

    // Connected viewers
    private Set<Viewer> viewersToAcknowledge;

    // World information
    private IndexedWorldModel world;
    private Set<RescueEntity> initialEntities;
    private int freezeTime;

    /** Lock object agent stuff. */
    private final Object agentLock = new Object();
    private final Object simLock = new Object();
    private final Object viewerLock = new Object();

    /**
       Create a LegacyComponentManager.
       @param kernel The kernel.
       @param world The world model.
       @param config The kernel configuration.
    */
    public LegacyComponentManager(Kernel kernel, IndexedWorldModel world, Config config) {
        this.kernel = kernel;
        this.world = world;
        civ = new LinkedList<Civilian>();
        fb = new LinkedList<FireBrigade>();
        fs = new LinkedList<FireStation>();
        at = new LinkedList<AmbulanceTeam>();
        ac = new LinkedList<AmbulanceCentre>();
        pf = new LinkedList<PoliceForce>();
        po = new LinkedList<PoliceOffice>();
        initialEntities = new HashSet<RescueEntity>();
        freezeTime = config.getIntValue("steps_agents_frozen", 0);
        for (RescueEntity e : world.getAllEntities()) {
            if (e instanceof Civilian) {
                civ.add((Civilian)e);
            }
            else if (e instanceof FireBrigade) {
                fb.add((FireBrigade)e);
            }
            else if (e instanceof FireStation) {
                fs.add((FireStation)e);
            }
            else if (e instanceof AmbulanceTeam) {
                at.add((AmbulanceTeam)e);
            }
            else if (e instanceof AmbulanceCentre) {
                ac.add((AmbulanceCentre)e);
            }
            else if (e instanceof PoliceForce) {
                pf.add((PoliceForce)e);
            }
            else if (e instanceof PoliceOffice) {
                po.add((PoliceOffice)e);
            }
            maybeAddInitialEntity(e);
        }

        agentsToAcknowledge = new HashSet<LegacyAgent>();
        simsToAcknowledge = new HashSet<LegacySimulator>();
        viewersToAcknowledge = new HashSet<Viewer>();
        nextSimulatorID = 1;
    }

    /**
       Wait for all agents to connect. This method will block until all agent entities have controllers.
       @throws InterruptedException If the thread is interrupted.
    */
    public void waitForAllAgents() throws InterruptedException {
        synchronized (agentLock) {
            while (!civ.isEmpty()
                   || !fb.isEmpty()
                   || !fs.isEmpty()
                   || !at.isEmpty()
                   || !ac.isEmpty()
                   || !pf.isEmpty()
                   || !po.isEmpty()
                   || !agentsToAcknowledge.isEmpty()) {
                agentLock.wait(WAIT_TIME);
                System.out.println("Waiting for " + civ.size() + " civilians, "
                                   + fb.size() + " fire brigades, "
                                   + fs.size() + " fire stations, "
                                   + at.size() + " ambulance teams, "
                                   + ac.size() + " ambulance centres, "
                                   + pf.size() + " police forces, "
                                   + po.size() + " police offices, "
                                   + agentsToAcknowledge.size() + " to acknowledge, ");
            }
        }
    }

    /**
       Wait until all simulators have acknowledged.
       @throws InterruptedException If the thread is interrupted.
    */
    public void waitForAllSimulators() throws InterruptedException {
        synchronized (simLock) {
            while (!simsToAcknowledge.isEmpty()) {
                simLock.wait(WAIT_TIME);
                System.out.println("Waiting for " + simsToAcknowledge.size() + " simulators to acknowledge");
            }
        }
    }

    /**
       Wait until all viewers have acknowledged.
       @throws InterruptedException If the thread is interrupted.
    */
    public void waitForAllViewers() throws InterruptedException {
        synchronized (viewerLock) {
            while (!viewersToAcknowledge.isEmpty()) {
                viewerLock.wait(WAIT_TIME);
                System.out.println("Waiting for " + viewersToAcknowledge.size() + " viewers to acknowledge");
            }
        }
    }

    @Override
    public void newConnection(Connection c) {
        c.addConnectionListener(new LegacyConnectionListener());
    }

    private boolean agentAcknowledge(int id, Connection c) {
        synchronized (agentLock) {
            for (LegacyAgent next : agentsToAcknowledge) {
                if (next.getControlledEntity().getID().getValue() == id && next.getConnection() == c) {
                    agentsToAcknowledge.remove(next);
                    kernel.addAgent(next);
                    agentLock.notifyAll();
                    return true;
                }
            }
            return false;
        }
    }

    private boolean simAcknowledge(int id, Connection c) {
        synchronized (simLock) {
            for (LegacySimulator next : simsToAcknowledge) {
                if (next.getID() == id && next.getConnection() == c) {
                    simsToAcknowledge.remove(next);
                    kernel.addSimulator(next);
                    simLock.notifyAll();
                    return true;
                }
            }
            return false;
        }
    }

    private boolean viewerAcknowledge(Connection c) {
        synchronized (viewerLock) {
            for (Viewer next : viewersToAcknowledge) {
                if (next.getConnection() == c) {
                    viewersToAcknowledge.remove(next);
                    kernel.addViewer(next);
                    viewerLock.notifyAll();
                    return true;
                }
            }
            return false;
        }
    }

    private int getNextID() {
        synchronized (simLock) {
            return nextSimulatorID++;
        }
    }

    private void maybeAddInitialEntity(RescueEntity e) {
        if (e instanceof Road) {
            Road r = (Road)e.copy();
            filterRoadProperties(r);
            initialEntities.add(r);
        }
        if (e instanceof Node) {
            Node n = (Node)e.copy();
            filterNodeProperties(n);
            initialEntities.add(n);
        }
        if (e instanceof Building) {
            Building b = (Building)e.copy();
            filterBuildingProperties(b);
            initialEntities.add(b);
        }
    }

    private void filterRoadProperties(Road r) {
        for (Property next : r.getProperties()) {
            // Road properties: ROAD_KIND, WIDTH, MEDIAN_STRIP, LINES_TO_HEAD, LINES_TO_TAIL and WIDTH_FOR_WALKERS
            // Edge properties: HEAD, TAIL, LENGTH
            // Everything else should be undefined
            switch ((RescuePropertyType)next.getType()) {
            case ROAD_KIND:
            case WIDTH:
            case MEDIAN_STRIP:
            case LINES_TO_HEAD:
            case LINES_TO_TAIL:
            case WIDTH_FOR_WALKERS:
            case HEAD:
            case TAIL:
            case LENGTH:
                break;
            default:
                next.undefine();
            }
        }
    }

    private void filterNodeProperties(Node n) {
        for (Property next : n.getProperties()) {
            // Node properties: SIGNAL, SHORTCUT_TO_TURN, POCKET_TO_TURN_ACROSS, SIGNAL_TIMING
            // Vertex properties: X, Y, EDGES
            // Everything else should be undefined
            switch ((RescuePropertyType)next.getType()) {
            case SIGNAL:
            case SHORTCUT_TO_TURN:
            case POCKET_TO_TURN_ACROSS:
            case SIGNAL_TIMING:
            case X:
            case Y:
            case EDGES:
                break;
            default:
                next.undefine();
            }
        }
    }

    private void filterBuildingProperties(Building b) {
        for (Property next : b.getProperties()) {
            // Building properties: X, Y, FLOORS, BUILDING_CODE, BUILDING_ATTRIBUTES, BUILDING_AREA_GROUND, BUILDING_AREA_TOTAL, IMPORTANCE, ENTRANCES
            // Everything else should be undefined
            switch ((RescuePropertyType)next.getType()) {
            case X:
            case Y:
            case FLOORS:
            case BUILDING_CODE:
            case BUILDING_ATTRIBUTES:
            case BUILDING_AREA_GROUND:
            case BUILDING_AREA_TOTAL:
            case IMPORTANCE:
            case ENTRANCES:
                break;
            default:
                next.undefine();
            }
        }
    }

    private RescueEntity findEntityToControl(int mask) {
        List<Queue<? extends RescueEntity>> toTry = new ArrayList<Queue<? extends RescueEntity>>();
        if ((mask & Constants.AGENT_TYPE_CIVILIAN) == Constants.AGENT_TYPE_CIVILIAN) {
            toTry.add(civ);
        }
        if ((mask & Constants.AGENT_TYPE_FIRE_BRIGADE) == Constants.AGENT_TYPE_FIRE_BRIGADE) {
            toTry.add(fb);
        }
        if ((mask & Constants.AGENT_TYPE_FIRE_STATION) == Constants.AGENT_TYPE_FIRE_STATION) {
            toTry.add(fs);
        }
        if ((mask & Constants.AGENT_TYPE_AMBULANCE_TEAM) == Constants.AGENT_TYPE_AMBULANCE_TEAM) {
            toTry.add(at);
        }
        if ((mask & Constants.AGENT_TYPE_AMBULANCE_CENTRE) == Constants.AGENT_TYPE_AMBULANCE_CENTRE) {
            toTry.add(ac);
        }
        if ((mask & Constants.AGENT_TYPE_POLICE_FORCE) == Constants.AGENT_TYPE_POLICE_FORCE) {
            toTry.add(pf);
        }
        if ((mask & Constants.AGENT_TYPE_POLICE_OFFICE) == Constants.AGENT_TYPE_POLICE_OFFICE) {
            toTry.add(po);
        }
        for (Queue<? extends RescueEntity> next : toTry) {
            RescueEntity e = next.poll();
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    private class LegacyConnectionListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection connection, Message msg) {
            if (msg instanceof AKConnect) {
                handleAKConnect((AKConnect)msg, connection);
            }
            if (msg instanceof AKAcknowledge) {
                handleAKAcknowledge((AKAcknowledge)msg, connection);
            }
            if (msg instanceof SKConnect) {
                handleSKConnect((SKConnect)msg, connection);
            }
            if (msg instanceof SKAcknowledge) {
                handleSKAcknowledge((SKAcknowledge)msg, connection);
            }
            if (msg instanceof VKConnect) {
                handleVKConnect((VKConnect)msg, connection);
            }
            if (msg instanceof VKAcknowledge) {
                handleVKAcknowledge((VKAcknowledge)msg, connection);
            }
        }

        private void handleAKConnect(AKConnect connect, Connection connection) {
            // Pull out the temp ID and agent type mask
            int tempID = connect.getTemporaryID();
            int mask = connect.getAgentTypeMask();
            // See if we can find an entity for this agent to control.
            synchronized (agentLock) {
                RescueEntity entity = findEntityToControl(mask);
                try {
                    if (entity == null) {
                        // Send an error
                        connection.sendMessage(new KAConnectError(tempID, "No more agents"));
                    }
                    else {
                        LegacyAgent agent = new LegacyAgent(entity, connection, freezeTime);
                        agentsToAcknowledge.add(agent);
                        // Send an OK
                        agent.send(Collections.singleton(new KAConnectOK(tempID, agent.getControlledEntity().getID().getValue(), (RescueEntity)agent.getControlledEntity(), initialEntities)));
                    }
                }
                catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleAKAcknowledge(AKAcknowledge msg, Connection connection) {
            int id = msg.getAgentID();
            if (agentAcknowledge(id, connection)) {
                System.out.println("Agent " + id + " acknowledged");
            }
            else {
                System.out.println("Unexpected acknowledge from agent " + id);
            }
        }

        private void handleSKConnect(SKConnect msg, Connection connection) {
            int id = getNextID();
            System.out.println("Simulator " + id + " connected");
            LegacySimulator sim = new LegacySimulator(connection, id);
            synchronized (simLock) {
                simsToAcknowledge.add(sim);
            }
            // Send an OK
            sim.send(Collections.singleton(new KSConnectOK(id, world.getAllEntities())));
        }

        private void handleSKAcknowledge(SKAcknowledge msg, Connection connection) {
            int id = msg.getSimulatorID();
            if (simAcknowledge(id, connection)) {
                System.out.println("Simulator " + id + " acknowledged");
            }
            else {
                System.out.println("Unexpected acknowledge from simulator " + id);
            }
        }

        private void handleVKConnect(VKConnect msg, Connection connection) {
            System.out.println("Viewer connected");
            Viewer viewer = new DefaultViewer(connection);
            synchronized (viewerLock) {
                viewersToAcknowledge.add(viewer);
            }
            // Send an OK
            viewer.send(Collections.singleton(new KVConnectOK(world.getAllEntities())));
        }

        private void handleVKAcknowledge(VKAcknowledge msg, Connection connection) {
            if (viewerAcknowledge(connection)) {
                System.out.println("Viewer acknowledged");
            }
            else {
                System.out.println("Unexpected viewer acknowledge");
            }
        }
    }
}