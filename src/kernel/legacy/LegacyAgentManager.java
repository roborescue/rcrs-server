package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import kernel.AbstractAgentManager;
import kernel.Agent;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.EntityID;
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
import rescuecore2.version0.messages.KASense;
import rescuecore2.version0.messages.AgentCommand;
import rescuecore2.version0.messages.AKSay;
import rescuecore2.version0.messages.AKTell;

/**
   AgentManager implementation for classic Robocup Rescue.
 */
public class LegacyAgentManager extends AbstractAgentManager<RescueEntity, IndexedWorldModel> {
    private Queue<Civilian> civ;
    private Queue<FireBrigade> fb;
    private Queue<FireStation> fs;
    private Queue<AmbulanceTeam> at;
    private Queue<AmbulanceCentre> ac;
    private Queue<PoliceForce> pf;
    private Queue<PoliceOffice> po;

    private Set<Agent<RescueEntity>> toAcknowledge;

    private Set<RescueEntity> initialEntities;

    private int freezeTime;

    /** Lock object for when new agents connect and choose an entity to control. */
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
        toAcknowledge = new HashSet<Agent<RescueEntity>>();
        initialEntities = new HashSet<RescueEntity>();
        freezeTime = config.getIntValue("steps_agents_frozen", 0);
    }

    @Override
    protected void processNewWorldModel(IndexedWorldModel world) {
        civ.clear();
        fb.clear();
        fs.clear();
        at.clear();
        ac.clear();
        pf.clear();
        po.clear();
        toAcknowledge.clear();
        initialEntities.clear();
        for (RescueEntity e : world.getAllEntities()) {
            if (e instanceof Civilian) {
                civ.add((Civilian)e);
                addControlledEntity(e);
            }
            else if (e instanceof FireBrigade) {
                fb.add((FireBrigade)e);
                addControlledEntity(e);
            }
            else if (e instanceof FireStation) {
                fs.add((FireStation)e);
                addControlledEntity(e);
            }
            else if (e instanceof AmbulanceTeam) {
                at.add((AmbulanceTeam)e);
                addControlledEntity(e);
            }
            else if (e instanceof AmbulanceCentre) {
                ac.add((AmbulanceCentre)e);
                addControlledEntity(e);
            }
            else if (e instanceof PoliceForce) {
                pf.add((PoliceForce)e);
                addControlledEntity(e);
            }
            else if (e instanceof PoliceOffice) {
                po.add((PoliceOffice)e);
                addControlledEntity(e);
            }
            maybeAddInitialEntity(e);
        }
    }

    @Override
    public void newConnection(Connection c) {
        c.addConnectionListener(new AgentConnectionListener());
    }

    private RescueEntity findEntityToControl(int mask) {
        synchronized (lock) {
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
    }

    private boolean acknowledge(int id, Connection c) {
        Agent<RescueEntity> agent = null;
        synchronized (lock) {
            for (Agent<RescueEntity> next : toAcknowledge) {
                if (next.getControlledEntity().getID().getValue() == id && next.getConnection() == c) {
                    agent = next;
                    toAcknowledge.remove(next);
                    break;
                }
            }
        }
        if (agent == null) {
            return false;
        }
        addAgent(agent);
        return true;
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

    private class AgentConnectionListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection connection, Message msg) {
            if (msg instanceof AKConnect) {
                // Pull out the temp ID and agent type mask
                AKConnect connect = (AKConnect)msg;
                int tempID = connect.getTemporaryID();
                int mask = connect.getAgentTypeMask();
                // See if we can find an entity for this agent to control.
                Agent<RescueEntity> agent = null;
                synchronized (lock) {
                    RescueEntity entity = findEntityToControl(mask);
                    if (entity != null) {
                        agent = new LegacyAgent(entity, connection, freezeTime);
                        toAcknowledge.add(agent);
                    }
                }
                try {
                    if (agent == null) {
                        // Send an error
                        connection.sendMessage(new KAConnectError(tempID, "No more agents"));
                    }
                    else {
                        // Send an OK
                        connection.sendMessage(new KAConnectOK(tempID, agent.getControlledEntity().getID().getValue(), agent.getControlledEntity(), initialEntities));
                    }
                }
                catch (ConnectionException e) {
                    e.printStackTrace();
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
        }
    }
}