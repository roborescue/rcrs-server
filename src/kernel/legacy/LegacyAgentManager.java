package kernel.legacy;

import java.util.Set;
import java.util.HashSet;

import kernel.AgentManager;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.entities.legacy.Civilian;
import rescuecore2.worldmodel.entities.legacy.FireBrigade;
import rescuecore2.worldmodel.entities.legacy.AmbulanceTeam;
import rescuecore2.worldmodel.entities.legacy.PoliceForce;

/**
   AgentManager implementation for classic Robocup Rescue.
 */
public class LegacyAgentManager implements AgentManager {
    private WorldModel worldModel;
    private Set<Civilian> civs;
    private Set<FireBrigade> fire;
    private Set<AmbulanceTeam> ambulance;
    private Set<PoliceForce> police;

    public LegacyAgentManager() {
	civs = new HashSet<Civilian>();
	fire = new HashSet<FireBrigade>();
	ambulance = new HashSet<AmbulanceTeam>();
	police = new HashSet<PoliceForce>();
    }

    @Override
    public void setWorldModel(WorldModel m) {
	worldModel = m;
	civs.clear();
	fire.clear();
	ambulance.clear();
	police.clear();
	for (Entity e : worldModel.getAllEntities()) {
	    if (e instanceof Civilian) {
		civs.add((Civilian)e);
	    }
	    else if (e instanceof FireBrigade) {
		fire.add((FireBrigade)e);
	    }
	    else if (e instanceof AmbulanceTeam) {
		ambulance.add((AmbulanceTeam)e);
	    }
	    else if (e instanceof PoliceForce) {
		police.add((PoliceForce)e);
	    }
	}
    }

    @Override
    public void newConnection(Connection c) {
	c.addConnectionListener(new AgentConnectionListener());
    }

    private class AgentConnectionListener implements ConnectionListener {
	@Override
	public void messageReceived(Message msg) {
	    if (msg instanceof AKConnect) {
	    }
	}
    }
}