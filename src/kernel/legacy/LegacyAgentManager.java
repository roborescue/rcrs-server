package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.io.IOException;

import kernel.AgentManager;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.version0.entities.RescueObject;
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
import rescuecore2.version0.entities.properties.PropertyType;
import rescuecore2.version0.messages.AKConnect;
import rescuecore2.version0.messages.AKAcknowledge;
import rescuecore2.version0.messages.KAConnectError;
import rescuecore2.version0.messages.KAConnectOK;
import rescuecore2.version0.messages.KASense;
import rescuecore2.version0.messages.AgentCommand;
import rescuecore2.version0.messages.Commands;
import rescuecore2.version0.messages.AKSay;
import rescuecore2.version0.messages.AKTell;

/**
   AgentManager implementation for classic Robocup Rescue.
 */
public class LegacyAgentManager implements AgentManager<RescueObject, IndexedWorldModel> {
    private IndexedWorldModel worldModel;
    private Queue<Civilian> civ;
    private Queue<FireBrigade> fb;
    private Queue<FireStation> fs;
    private Queue<AmbulanceTeam> at;
    private Queue<AmbulanceCentre> ac;
    private Queue<PoliceForce> pf;
    private Queue<PoliceOffice> po;

    private Set<AgentInfo> toAcknowledge;

    private Set<RescueObject> initialEntities;

    private Set<RescueObject> controlledEntities;
    private Map<RescueObject, AgentInfo> agents;

    private Map<EntityID, List<Message>> agentCommands;

    private int freezeTime;

    private final Object lock = new Object();

    /**
       Create a LegacyAgentManager.
       @param config The kernel configuration.
    */
    public LegacyAgentManager(Config config) {
        civ = new LinkedList<Civilian>();
        fb = new LinkedList<FireBrigade>();
        fs = new LinkedList<FireStation>();
        at = new LinkedList<AmbulanceTeam>();
        ac = new LinkedList<AmbulanceCentre>();
        pf = new LinkedList<PoliceForce>();
        po = new LinkedList<PoliceOffice>();
        toAcknowledge = new HashSet<AgentInfo>();
        initialEntities = new HashSet<RescueObject>();
        controlledEntities = new HashSet<RescueObject>();
        agents = new HashMap<RescueObject, AgentInfo>();
        agentCommands = new HashMap<EntityID, List<Message>>();
        freezeTime = config.getIntValue("steps_agents_frozen", 0);
    }

    @Override
    public void setWorldModel(IndexedWorldModel world) {
        worldModel = world;
        civ.clear();
        fb.clear();
        fs.clear();
        at.clear();
        ac.clear();
        pf.clear();
        po.clear();
        toAcknowledge.clear();
        initialEntities.clear();
        agents.clear();
        for (RescueObject e : worldModel.getAllEntities()) {
            if (e instanceof Civilian) {
                civ.add((Civilian)e);
                controlledEntities.add(e);
            }
            else if (e instanceof FireBrigade) {
                fb.add((FireBrigade)e);
                controlledEntities.add(e);
            }
            else if (e instanceof FireStation) {
                fs.add((FireStation)e);
                controlledEntities.add(e);
            }
            else if (e instanceof AmbulanceTeam) {
                at.add((AmbulanceTeam)e);
                controlledEntities.add(e);
            }
            else if (e instanceof AmbulanceCentre) {
                ac.add((AmbulanceCentre)e);
                controlledEntities.add(e);
            }
            else if (e instanceof PoliceForce) {
                pf.add((PoliceForce)e);
                controlledEntities.add(e);
            }
            else if (e instanceof PoliceOffice) {
                po.add((PoliceOffice)e);
                controlledEntities.add(e);
            }
            maybeAddInitialEntity(e);
        }
    }

    @Override
    public void newConnection(Connection c) {
        c.addConnectionListener(new AgentConnectionListener(c));
    }

    @Override
    public void waitForAllAgents() throws InterruptedException {
        synchronized (lock) {
            while (!civ.isEmpty()
                   || !fb.isEmpty()
                   || !fs.isEmpty()
                   || !at.isEmpty()
                   || !ac.isEmpty()
                   || !po.isEmpty()
                   || !pf.isEmpty()
                   || !toAcknowledge.isEmpty()) {
                lock.wait(1000);
                System.out.println("Waiting for " + civ.size() + " civilians, "
                                   + fb.size() + " fire brigades, "
                                   + fs.size() + " fire stations, "
                                   + at.size() + " ambulance teams, "
                                   + ac.size() + " ambulance centres, "
                                   + pf.size() + " police forces, "
                                   + po.size() + " police offices. "
                                   + toAcknowledge.size() + " agents have not acknowledged.");
            }
        }
    }

    @Override
    public Set<RescueObject> getControlledEntities() {
        return controlledEntities;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void sendPerceptionUpdate(int time, RescueObject agent, Collection<RescueObject> visible) {
        AgentInfo info = agents.get(agent);
        if (info == null) {
            throw new IllegalArgumentException("Unrecognised object: " + agent);
        }
        KASense sense = new KASense(agent.getID().getValue(), time, visible);
        info.send(Collections.singleton(sense));
    }

    @Override
    public void sendMessages(RescueObject agent, Collection<Message> messages) {
        AgentInfo info = agents.get(agent);
        if (info == null) {
            throw new IllegalArgumentException("Unrecognised object: " + agent);
        }
        info.send(messages);
    }

    @Override
    public Collection<Message> getAgentCommands(int timestep) {
        Collection<Message> commands = new ArrayList<Message>();
        synchronized (agentCommands) {
            for (List<Message> list : agentCommands.values()) {
                for (Message next : list) {
                    if (next instanceof AgentCommand) {
                        commands.add((AgentCommand)next);
                    }
                }
            }
            agentCommands.clear();
        }
        if (timestep < freezeTime) {
            // Only allow say and tell commands if it's too early
            for (Iterator<Message> it = commands.iterator(); it.hasNext();) {
                Message next = it.next();
                if (!(next instanceof AKSay || next instanceof AKTell)) {
                    System.out.println("Ignoring " + next + ": " + timestep + " < " + freezeTime);
                    it.remove();
                }
            }
        }
        return commands;
    }

    private AgentInfo findEntityToControl(int mask) {
        synchronized (lock) {
            List<Queue<? extends RescueObject>> toTry = new ArrayList<Queue<? extends RescueObject>>();
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
            for (Queue<? extends RescueObject> next : toTry) {
                RescueObject e = next.poll();
                if (e != null) {
                    AgentInfo info = new AgentInfo(e);
                    toAcknowledge.add(info);
                    lock.notifyAll();
                    return info;
                }
            }
            return null;
        }
    }

    private boolean acknowledge(int id, Connection c) {
        synchronized (lock) {
            for (AgentInfo next : toAcknowledge) {
                if (next.entity.getID().getValue() == id && next.connection == c) {
                    toAcknowledge.remove(next);
                    agents.put(next.entity, next);
                    lock.notifyAll();
                    return true;
                }
            }
            return false;
        }
    }

    private Collection<RescueObject> getInitialEntityList() {
        return initialEntities;
    }

    private void maybeAddInitialEntity(RescueObject e) {
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
            switch (PropertyType.fromID(next.getID())) {
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
            switch (PropertyType.fromID(next.getID())) {
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
            switch (PropertyType.fromID(next.getID())) {
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

    private void agentCommandReceived(EntityID id, Message message) {
        synchronized (agentCommands) {
            List<Message> messages = agentCommands.get(id);
            if (messages == null) {
                messages = new ArrayList<Message>();
                agentCommands.put(id, messages);
            }
            messages.add(message);
        }
    }

    private class AgentConnectionListener implements ConnectionListener {
        private Connection connection;

        public AgentConnectionListener(Connection c) {
            connection = c;
        }

        @Override
        public void messageReceived(Message msg) {
            if (msg instanceof AKConnect) {
                // Pull out the temp ID and agent type mask
                AKConnect connect = (AKConnect)msg;
                int tempID = connect.getTemporaryID();
                int mask = connect.getAgentTypeMask();
                // See if we can find an entity for this agent to control.
                AgentInfo info = findEntityToControl(mask);
                try {
                    if (info == null) {
                        // Send an error
                        connection.sendMessage(new KAConnectError(tempID, "No more agents"));
                    }
                    else {
                        info.connection = connection;
                        // Send an OK
                        connection.sendMessage(new KAConnectOK(tempID, info.entity.getID().getValue(), info.entity, getInitialEntityList()));
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    info.dead = true;
                }
                catch (ConnectionException e) {
                    e.printStackTrace();
                    info.dead = true;
                }
            }
            if (msg instanceof AKAcknowledge) {
                int id = ((AKAcknowledge)msg).getAgentID();
                if (acknowledge(id, connection)) {
                    System.out.println("Agent " + id + " acknowledged");
                }
                else {
                    System.out.println("Unexpected acknowledge from agent " + id);
                }
            }
            if (msg instanceof AgentCommand) {
                EntityID id = ((AgentCommand)msg).getAgentID();
                agentCommandReceived(id, msg);
            }
        }
    }

    private static class AgentInfo {
        RescueObject entity;
        Connection connection;
        boolean dead;

        AgentInfo(RescueObject entity) {
            this.entity = entity;
            dead = false;
        }

        void send(Collection<? extends Message> m) {
            if (dead) {
                return;
            }
            try {
                connection.sendMessages(m);
            }
            catch (IOException e) {
                e.printStackTrace();
                dead = true;
            }
            catch (ConnectionException e) {
                e.printStackTrace();
                dead = true;
            }
        }
    }
}