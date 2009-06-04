package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import kernel.Kernel;
import kernel.Agent;
import kernel.Viewer;
//import kernel.Simulator;
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
import rescuecore2.messages.control.AKConnect;
import rescuecore2.messages.control.AKAcknowledge;
import rescuecore2.messages.control.KAConnectError;
import rescuecore2.messages.control.KAConnectOK;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.Property;

import rescuecore2.version0.entities.RescueWorldModel;
import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.entities.RescueEntityType;
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

/**
   Class that manages connecting legacy components.
 */
public class LegacyComponentManager implements ConnectionManagerListener {
    private static final int WAIT_TIME = 10000;

    private Kernel kernel;

    // Entities that have no controller yet. Map from type to list of entities.
    private Map<Integer, Queue<Entity>> uncontrolledEntities;

    /*
      private Queue<Civilian> civ;
      private Queue<FireBrigade> fb;
      private Queue<FireStation> fs;
      private Queue<AmbulanceTeam> at;
      private Queue<AmbulanceCentre> ac;
      private Queue<PoliceForce> pf;
      private Queue<PoliceOffice> po;
    */

    // Connected agents
    private Set<AgentAck> agentsToAcknowledge;

    // Connected simulators
    private Set<LegacySimulator> simsToAcknowledge;
    private int nextSimulatorID;

    // Connected viewers
    private Set<Viewer> viewersToAcknowledge;

    // World information
    private RescueWorldModel world;
    private Set<RescueEntity> initialEntities;

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
    public LegacyComponentManager(Kernel kernel, RescueWorldModel world, Config config) {
        this.kernel = kernel;
        this.world = world;
        uncontrolledEntities = new HashMap<Integer, Queue<Entity>>();
        Queue<Entity> civ = new LinkedList<Entity>();
        Queue<Entity> fb = new LinkedList<Entity>();
        Queue<Entity> fs = new LinkedList<Entity>();
        Queue<Entity> at = new LinkedList<Entity>();
        Queue<Entity> ac = new LinkedList<Entity>();
        Queue<Entity> pf = new LinkedList<Entity>();
        Queue<Entity> po = new LinkedList<Entity>();
        uncontrolledEntities.put(RescueEntityType.CIVILIAN.getID(), civ);
        uncontrolledEntities.put(RescueEntityType.FIRE_BRIGADE.getID(), fb);
        uncontrolledEntities.put(RescueEntityType.FIRE_STATION.getID(), fs);
        uncontrolledEntities.put(RescueEntityType.AMBULANCE_TEAM.getID(), at);
        uncontrolledEntities.put(RescueEntityType.AMBULANCE_CENTRE.getID(), ac);
        uncontrolledEntities.put(RescueEntityType.POLICE_FORCE.getID(), pf);
        uncontrolledEntities.put(RescueEntityType.POLICE_OFFICE.getID(), po);
        /*
          civ = new LinkedList<Civilian>();
          fb = new LinkedList<FireBrigade>();
          fs = new LinkedList<FireStation>();
          at = new LinkedList<AmbulanceTeam>();
          ac = new LinkedList<AmbulanceCentre>();
          pf = new LinkedList<PoliceForce>();
          po = new LinkedList<PoliceOffice>();
        */
        initialEntities = new HashSet<RescueEntity>();
        for (Entity e : world.getAllEntities()) {
            if (e instanceof Civilian) {
                civ.add(e);
            }
            else if (e instanceof FireBrigade) {
                fb.add(e);
            }
            else if (e instanceof FireStation) {
                fs.add(e);
            }
            else if (e instanceof AmbulanceTeam) {
                at.add(e);
            }
            else if (e instanceof AmbulanceCentre) {
                ac.add(e);
            }
            else if (e instanceof PoliceForce) {
                pf.add(e);
            }
            else if (e instanceof PoliceOffice) {
                po.add(e);
            }
            maybeAddInitialEntity(e);
        }

        agentsToAcknowledge = new HashSet<AgentAck>();
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
            boolean done = false;
            do {
                done = true;
                for (Map.Entry<Integer, Queue<Entity>> next : uncontrolledEntities.entrySet()) {
                    if (!next.getValue().isEmpty()) {
                        done = false;
                        System.out.println("Waiting for " + next.getValue().size() + " entities of type " + next.getKey());
                    }
                }
                if (!agentsToAcknowledge.isEmpty()) {
                    done = false;
                    System.out.println("Waiting for " + agentsToAcknowledge.size() + " agents to acknowledge");
                }
                if (!done) {
                    agentLock.wait(WAIT_TIME);
                }
            } while (!done);
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

    private boolean agentAcknowledge(int requestID, Connection c) {
        synchronized (agentLock) {
            for (AgentAck next : agentsToAcknowledge) {
                if (next.requestID == requestID && next.connection == c) {
                    agentsToAcknowledge.remove(next);
                    kernel.addAgent(next.agent);
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

    private void maybeAddInitialEntity(Entity e) {
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

    private Entity findEntityToControl(List<Integer> types) {
        for (int next : types) {
            Queue<Entity> q = uncontrolledEntities.get(next);
            if (q != null) {
                Entity e = q.poll();
                if (e != null) {
                    return e;
                }
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
            // Pull out the request ID and requested entity type list
            int requestID = connect.getRequestID();
            List<Integer> types = connect.getRequestedEntityTypes();
            // See if we can find an entity for this agent to control.
            Message reply = null;
            synchronized (agentLock) {
                Entity entity = findEntityToControl(types);
                if (entity == null) {
                    // Send an error
                    reply = new KAConnectError(requestID, "No more agents");
                }
                else {
                    Agent agent = new LegacyAgent(entity, connection);
                    agentsToAcknowledge.add(new AgentAck(agent, requestID, connection));
                    // Send an OK
                    Set<Entity> allEntities = new HashSet<Entity>(initialEntities);
                    allEntities.add(entity);
                    reply = new KAConnectOK(requestID, entity.getID(), allEntities);
                }
            }
            if (reply != null) {
                try {
                    connection.sendMessage(reply);
                }
                catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleAKAcknowledge(AKAcknowledge msg, Connection connection) {
            int id = msg.getRequestID();
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

    private static class AgentAck {
        Agent agent;
        int requestID;
        Connection connection;

        public AgentAck(Agent agent, int requestID, Connection c) {
            this.agent = agent;
            this.requestID = requestID;
            this.connection = c;
        }
    }
}