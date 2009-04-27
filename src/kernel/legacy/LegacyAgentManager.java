package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.io.IOException;

import kernel.AgentManager;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.version0.entities.Civilian;
import rescuecore2.version0.entities.FireBrigade;
import rescuecore2.version0.entities.FireStation;
import rescuecore2.version0.entities.AmbulanceTeam;
import rescuecore2.version0.entities.AmbulanceCentre;
import rescuecore2.version0.entities.PoliceForce;
import rescuecore2.version0.entities.PoliceOffice;
import rescuecore2.version0.messages.AKConnect;
import rescuecore2.version0.messages.AKAcknowledge;
import rescuecore2.version0.messages.KAConnectError;
import rescuecore2.version0.messages.KAConnectOK;

/**
   AgentManager implementation for classic Robocup Rescue.
 */
public class LegacyAgentManager implements AgentManager {
    private WorldModel worldModel;
    private Queue<Civilian> civ;
    private Queue<FireBrigade> fb;
    private Queue<FireStation> fs;
    private Queue<AmbulanceTeam> at;
    private Queue<AmbulanceCentre> ac;
    private Queue<PoliceForce> pf;
    private Queue<PoliceOffice> po;

    private Set<AgentInfo> toAcknowledge;

    private final Object AGENT_LOCK = new Object();

    public LegacyAgentManager(WorldModel m) {
	worldModel = m;
	civ = new LinkedList<Civilian>();
	fb = new LinkedList<FireBrigade>();
	fs = new LinkedList<FireStation>();
	at = new LinkedList<AmbulanceTeam>();
	ac = new LinkedList<AmbulanceCentre>();
	pf = new LinkedList<PoliceForce>();
	po = new LinkedList<PoliceOffice>();
        toAcknowledge = new HashSet<AgentInfo>();
	for (Entity e : worldModel.getAllEntities()) {
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
	}
    }

    @Override
    public void newConnection(Connection c) {
	c.addConnectionListener(new AgentConnectionListener(c));
    }

    @Override
    public void waitForAllAgents() throws InterruptedException {
        synchronized (AGENT_LOCK) {
            while (!civ.isEmpty() ||
                   !fb.isEmpty() ||
                   !fs.isEmpty() ||
                   !at.isEmpty() ||
                   !ac.isEmpty() ||
                   !po.isEmpty() ||
                   !pf.isEmpty() ||
                   !toAcknowledge.isEmpty()) {
                AGENT_LOCK.wait(1000);
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

    private AgentInfo findEntityToControl(int mask) {
        synchronized (AGENT_LOCK) {
            List<Queue<? extends Entity>> toTry = new ArrayList<Queue<? extends Entity>>();
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
            for (Queue<? extends Entity> next : toTry) {
                Entity e = next.poll();
                if (e != null) {
                    AgentInfo info = new AgentInfo(e);
                    toAcknowledge.add(info);
                    AGENT_LOCK.notifyAll();
                    return info;
                }
            }
            return null;
        }
    }

    private boolean acknowledge(int id) {
        synchronized(AGENT_LOCK) {
            for (AgentInfo next : toAcknowledge) {
                if (next.entity.getID().getValue() == id) {
                    toAcknowledge.remove(next);
                    AGENT_LOCK.notifyAll();
                    return true;
                }
            }
            return false;
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
                        info.tempID = tempID;
                        info.connection = connection;
                        // Send an OK
                        connection.sendMessage(new KAConnectOK(tempID, info.entity.getID().getValue(), info.entity, worldModel.getAllEntities()));
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (msg instanceof AKAcknowledge) {
                int id = ((AKAcknowledge)msg).getAgentID();
                if (acknowledge(id)) {
                    System.out.println("Agent " + id + " acknowledged");
                }
                else {
                    System.out.println("Unexpected acknowledge from agent " + id);
                }
            }
	}
    }

    private static class AgentInfo {
        Entity entity;
        int tempID;
        Connection connection;

        public AgentInfo(Entity entity) {
            this.entity = entity;
        }
    }
}