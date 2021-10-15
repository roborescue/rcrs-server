package kernel;

import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.KSAfterShocksInfo;
import rescuecore2.messages.control.VKConnect;
import rescuecore2.registry.Registry;
import rescuecore2.messages.control.VKAcknowledge;
import rescuecore2.messages.control.KVConnectOK;
import rescuecore2.messages.control.SKConnect;
import rescuecore2.messages.control.SKAcknowledge;
import rescuecore2.messages.control.KSConnectOK;
import rescuecore2.messages.control.AKConnect;
import rescuecore2.messages.control.AKAcknowledge;
import rescuecore2.messages.control.KAConnectError;
import rescuecore2.messages.control.KAConnectOK;
import rescuecore2.scenario.Scenario;
import rescuecore2.scenario.exceptions.UncompatibleScenarioException;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.GUIComponent;
import rescuecore2.log.Logger;

import kernel.ui.ComponentManagerGUI;

import javax.swing.JComponent;

/**
 * Class that manages connecting components (agents, simulators, viewers) to the
 * kernel.
 */
public class ComponentManager implements ConnectionManagerListener,
		GUIComponent {
	private static final int STARTING_ID = 1;

	private static final int WAIT_TIME = 10000;

	private Kernel kernel;
	private ComponentManagerGUI gui;

	// Entities that have no controller yet. Map from type to list of entities.
	private Map<Integer, Queue<ControlledEntityInfo>> uncontrolledEntities;

	// Connected agents
	private Set<AgentAck> agentsToAcknowledge;

	// Connected simulators
	private Set<SimulatorAck> simsToAcknowledge;
	private int nextID;

	// Connected viewers
	private Set<ViewerAck> viewersToAcknowledge;

	// World information
	private WorldModel<? extends Entity> world;

	private Config config;

	/** Lock objects. */
	private final Object agentLock = new Object();
	private final Object simLock = new Object();
	private final Object viewerLock = new Object();
	private final Object idLock = new Object();

	private final Scenario scenario;

	/**
	 * Create a ComponentManager.
	 * 
	 * @param kernel
	 *            The kernel.
	 * @param world
	 *            The world model.
	 * @param config
	 *            The kernel configuration.
	 */
	public ComponentManager(Kernel kernel, WorldModel<? extends Entity> world,
			Config config, Scenario scenario) {
		this.kernel = kernel;
		this.world = world;
		this.config = config;
		this.scenario = scenario;
		uncontrolledEntities = new HashMap<Integer, Queue<ControlledEntityInfo>>();
		agentsToAcknowledge = new HashSet<AgentAck>();
		simsToAcknowledge = new HashSet<SimulatorAck>();
		viewersToAcknowledge = new HashSet<ViewerAck>();
		nextID = STARTING_ID;
		gui = new ComponentManagerGUI();
	}

	/**
	 * Register an agent-controlled entity.
	 * 
	 * @param entity
	 *            The entity that is agent-controlled.
	 * @param visibleOnStartup
	 *            The set of entities that the agent should be sent on startup.
	 *            If this is null then all entities will be sent.
	 * @param agentConfig
	 *            A view of the system configuration that should be shared with
	 *            the agent.
	 */
	public void registerAgentControlledEntity(Entity entity,
			Collection<? extends Entity> visibleOnStartup, Config agentConfig) {
		Logger.info("Agent controlled entity registered: " + entity);
		synchronized (agentLock) {
			Queue<ControlledEntityInfo> q = uncontrolledEntities.get(entity
					.getURN());
			if (q == null) {
				q = new LinkedList<ControlledEntityInfo>();
				uncontrolledEntities.put(entity.getURN(), q);
			}
			if (visibleOnStartup == null) {
				visibleOnStartup = world.getAllEntities();
			}
			q.add(new ControlledEntityInfo(entity, visibleOnStartup,
					agentConfig));
		}
		updateGUIUncontrolledAgents();
	}

	/**
	 * Wait for all agents to connect. This method will block until all agent
	 * entities have controllers.
	 * 
	 * @throws InterruptedException
	 *             If the thread is interrupted.
	 */
	public void waitForAllAgents() throws InterruptedException {
		synchronized (agentLock) {
			boolean done = false;
			do {
				done = true;
				for (Entry<Integer, Queue<ControlledEntityInfo>> next : uncontrolledEntities
						.entrySet()) {
					if (!next.getValue().isEmpty()) {
						done = false;
						Logger.info("Waiting for " + next.getValue().size()
								+ " entities of type " + next.getKey());
					}
				}
				if (!agentsToAcknowledge.isEmpty()) {
					done = false;
					Logger.info("Waiting for " + agentsToAcknowledge.size()
							+ " agents to acknowledge");
				}
				if (!done) {
					agentLock.wait(WAIT_TIME);
				}
			} while (!done);
		}
	}

	/**
	 * Wait until all simulators have acknowledged.
	 * 
	 * @throws InterruptedException
	 *             If the thread is interrupted.
	 */
	public void waitForAllSimulators() throws InterruptedException {
		synchronized (simLock) {
			while (!simsToAcknowledge.isEmpty()) {
				simLock.wait(WAIT_TIME);
				Logger.info("Waiting for " + simsToAcknowledge.size()
						+ " simulators to acknowledge");
			}
		}
	}

	/**
	 * Wait until all viewers have acknowledged.
	 * 
	 * @throws InterruptedException
	 *             If the thread is interrupted.
	 */
	public void waitForAllViewers() throws InterruptedException {
		synchronized (viewerLock) {
			while (!viewersToAcknowledge.isEmpty()) {
				viewerLock.wait(WAIT_TIME);
				Logger.info("Waiting for " + viewersToAcknowledge.size()
						+ " viewers to acknowledge");
			}
		}
	}

	@Override
	public void newConnection(Connection c) {
		c.addConnectionListener(new ComponentConnectionListener());
	}

	@Override
	public JComponent getGUIComponent() {
		return gui;
	}

	@Override
	public String getGUIComponentName() {
		return "Component manager";
	}

	private boolean agentAcknowledge(int requestID, EntityID agentID,
			Connection c) {
		synchronized (agentLock) {
			for (AgentAck next : agentsToAcknowledge) {
				if (next.requestID == requestID && next.agentID.equals(agentID)
						&& next.connection == c) {
					agentsToAcknowledge.remove(next);
					kernel.addAgent(next.agent);
					agentLock.notifyAll();
					return true;
				}
			}
			return false;
		}
	}

	private boolean simAcknowledge(int requestID, int simulatorID, Connection c) {
		synchronized (simLock) {
			for (SimulatorAck next : simsToAcknowledge) {
				if (next.requestID == requestID
						&& next.simulatorID == simulatorID
						&& next.connection == c) {
					simsToAcknowledge.remove(next);
					kernel.addSimulator(next.sim);
					simLock.notifyAll();
					return true;
				}
			}
			return false;
		}
	}

	private boolean viewerAcknowledge(int requestID, int viewerID, Connection c) {
		synchronized (viewerLock) {
			for (ViewerAck next : viewersToAcknowledge) {
				if (next.requestID == requestID && next.viewerID == viewerID
						&& next.connection == c) {
					viewersToAcknowledge.remove(next);
					kernel.addViewer(next.viewer);
					viewerLock.notifyAll();
					return true;
				}
			}
			return false;
		}
	}

	private int getNextSimulatorID() {
		synchronized (idLock) {
			return nextID++;
		}
	}

	private int getNextViewerID() {
		synchronized (idLock) {
			return nextID++;
		}
	}

	private ControlledEntityInfo findEntityToControl(List<Integer> types) {
		Logger.debug("Finding entity to control. Requested types: " + types);
		for (Integer next : types) {
			Queue<ControlledEntityInfo> q = uncontrolledEntities.get(next);
			Logger.debug("Uncontrolled entities of type " + next + ": " + q);
			if (q != null) {
				ControlledEntityInfo info = q.poll();
				if (info != null) {
					return info;
				}
			}
		}
		return null;
	}

	private void updateGUIUncontrolledAgents() {
		List<String> data = new ArrayList<String>();
		synchronized (agentLock) {
			for (Queue<ControlledEntityInfo> q : uncontrolledEntities.values()) {
				for (ControlledEntityInfo info : q) {
					data.add(Registry.SYSTEM_REGISTRY.toPrettyName(info.entity.getURN()) + " " + info.entity.getID());
				}
			}
		}
		gui.updateUncontrolledAgents(data);
	}

	private void updateGUIAgentAck() {
		List<String> data = new ArrayList<String>();
		synchronized (agentLock) {
			for (AgentAck ack : agentsToAcknowledge) {
				data.add(ack.toString());
			}
		}
		gui.updateAgentAck(data);
	}

	private void updateGUISimulatorAck() {
		List<String> data = new ArrayList<String>();
		synchronized (simLock) {
			for (SimulatorAck ack : simsToAcknowledge) {
				data.add(ack.toString());
			}
		}
		gui.updateSimulatorAck(data);
	}

	private void updateGUIViewerAck() {
		List<String> data = new ArrayList<String>();
		synchronized (viewerLock) {
			for (ViewerAck ack : viewersToAcknowledge) {
				data.add(ack.toString());
			}
		}
		gui.updateViewerAck(data);
	}

	private class ComponentConnectionListener implements ConnectionListener {
		@Override
		public void messageReceived(Connection connection, Message msg) {
			if (msg instanceof AKConnect) {
				handleAKConnect((AKConnect) msg, connection);
			}
			if (msg instanceof AKAcknowledge) {
				handleAKAcknowledge((AKAcknowledge) msg, connection);
			}
			try {
				if (msg instanceof SKConnect) {
					handleSKConnect((SKConnect) msg, connection);
				}
			} catch (UncompatibleScenarioException e) {
				e.printStackTrace();
			}
			if (msg instanceof SKAcknowledge) {
				handleSKAcknowledge((SKAcknowledge) msg, connection);
			}
			if (msg instanceof VKConnect) {
				handleVKConnect((VKConnect) msg, connection);
			}
			if (msg instanceof VKAcknowledge) {
				handleVKAcknowledge((VKAcknowledge) msg, connection);
			}
		}

		private void handleAKConnect(AKConnect connect, Connection connection) {
			// Pull out the request ID and requested entity type list
			int requestID = connect.getRequestID();
			List<Integer> types = connect.getRequestedEntityTypes();
			// See if we can find an entity for this agent to control.
			Message reply = null;
			Logger.debug("AKConnect received: " + types);
			synchronized (agentLock) {
				ControlledEntityInfo result = findEntityToControl(types);
				if (result == null) {
					Logger.debug("No suitable entities found");
					// Send an error
					reply = new KAConnectError(requestID, "No more agents");
				} else {
					Logger.debug("Found entity to control: " + result);
					Entity entity = result.entity;
					AgentProxy agent = new AgentProxy(connect.getAgentName(),
							entity, connection);
					agentsToAcknowledge.add(new AgentAck(agent, entity.getID(),
							requestID, connection));
					Logger.info("Agent '" + connect.getAgentName() + "' id "
							+ entity.getID() + " (" + connection
							+ " request ID " + requestID + ") connected");
					// Send an OK
					reply = new KAConnectOK(requestID, entity.getID(),
							result.visibleSet, result.config);
				}
			}
			if (reply != null) {
				try {
					connection.sendMessage(reply);
				} catch (ConnectionException e) {
					Logger.error("Error sending reply", e);
				}
			}
			updateGUIUncontrolledAgents();
			updateGUIAgentAck();
		}

		private void handleAKAcknowledge(AKAcknowledge msg,
				Connection connection) {
			int requestID = msg.getRequestID();
			EntityID agentID = msg.getAgentID();
			if (agentAcknowledge(requestID, agentID, connection)) {
				Logger.info("Agent " + agentID + " (" + connection
						+ " request ID " + requestID + ") acknowledged");
			} else {
				Logger.warn("Unexpected acknowledge from agent " + agentID
						+ " (request ID " + requestID + ")");
			}
			updateGUIAgentAck();
		}

		private void handleSKConnect(SKConnect msg, Connection connection)
				throws UncompatibleScenarioException {
			int simID = getNextSimulatorID();
			int requestID = msg.getRequestID();
			Logger.info("Simulator '" + msg.getSimulatorName() + "' id "
					+ simID + " (" + connection + " request ID " + requestID
					+ ") connected");
			SimulatorProxy sim = new SimulatorProxy(msg.getSimulatorName(),
					simID, connection);
			synchronized (simLock) {
				simsToAcknowledge.add(new SimulatorAck(sim, simID, requestID,
						connection));
			}
			// Send an OK
			sim.send(Collections.singleton(new KSConnectOK(simID, requestID,
					world.getAllEntities(), config)));
			sendAdditionalInfoToSim(sim);
			updateGUISimulatorAck();
		}

		/**
		 * Used to send info other than world model's to simulators. Information
		 * such as Aftershocks' properties and etc.
		 * 
		 * @param sim
		 * @throws UncompatibleScenarioException
		 */
		private void sendAdditionalInfoToSim(SimulatorProxy sim)
				throws UncompatibleScenarioException {
			sim.send(Collections.singleton(new KSAfterShocksInfo(scenario)));
		}

		private void handleSKAcknowledge(SKAcknowledge msg,
				Connection connection) {
			int requestID = msg.getRequestID();
			int simID = msg.getSimulatorID();
			if (simAcknowledge(requestID, simID, connection)) {
				Logger.info("Simulator " + simID + " (" + connection
						+ " request ID " + requestID + ") acknowledged");
			} else {
				Logger.warn("Unexpected acknowledge from simulator " + simID
						+ " (request ID " + requestID + ")");
			}
			updateGUISimulatorAck();
		}

		private void handleVKConnect(VKConnect msg, Connection connection) {
			int requestID = msg.getRequestID();
			int viewerID = getNextViewerID();
			Logger.info("Viewer '" + msg.getViewerName() + "' id " + viewerID
					+ " (" + connection + " request ID " + requestID
					+ ") connected");
			ViewerProxy viewer = new ViewerProxy(msg.getViewerName(), viewerID,
					connection);
			synchronized (viewerLock) {
				viewersToAcknowledge.add(new ViewerAck(viewer, viewerID,
						requestID, connection));
			}
			// Send an OK
			viewer.send(Collections.singleton(new KVConnectOK(viewerID,
					requestID, world.getAllEntities(), config)));
			updateGUIViewerAck();
		}

		private void handleVKAcknowledge(VKAcknowledge msg,
				Connection connection) {
			int requestID = msg.getRequestID();
			int viewerID = msg.getViewerID();
			if (viewerAcknowledge(requestID, viewerID, connection)) {
				Logger.info("Viewer " + viewerID + " (" + connection
						+ " request ID " + requestID + ") acknowledged");
			} else {
				Logger.warn("Unexpected acknowledge from viewer " + viewerID
						+ " (" + requestID + ")");
			}
			updateGUIViewerAck();
		}
	}

	private static class AgentAck {
		AgentProxy agent;
		EntityID agentID;
		int requestID;
		Connection connection;

		public AgentAck(AgentProxy agent, EntityID agentID, int requestID,
				Connection c) {
			this.agent = agent;
			this.agentID = agentID;
			this.requestID = requestID;
			this.connection = c;
		}

		@Override
		public String toString() {
			return agent.getName() + ": "
					+ Registry.SYSTEM_REGISTRY.toPrettyName(agent.getControlledEntity().getURN()) + " "
					+ agent.getControlledEntity().getID() + "(" + connection
					+ " request ID " + requestID + ")";
		}
	}

	private static class SimulatorAck {
		SimulatorProxy sim;
		int simulatorID;
		int requestID;
		Connection connection;

		public SimulatorAck(SimulatorProxy sim, int simID, int requestID,
				Connection c) {
			this.sim = sim;
			this.simulatorID = simID;
			this.requestID = requestID;
			this.connection = c;
		}

		@Override
		public String toString() {
			return sim + " " + simulatorID + "(connection request ID "
					+ requestID + ")";
		}
	}

	private static class ViewerAck {
		ViewerProxy viewer;
		int viewerID;
		int requestID;
		Connection connection;

		public ViewerAck(ViewerProxy viewer, int viewerID, int requestID,
				Connection c) {
			this.viewer = viewer;
			this.viewerID = viewerID;
			this.requestID = requestID;
			this.connection = c;
		}

		@Override
		public String toString() {
			return viewer + " " + viewerID + "(connection request ID "
					+ requestID + ")";
		}
	}

	private static class ControlledEntityInfo {
		Entity entity;
		Collection<? extends Entity> visibleSet;
		Config config;

		public ControlledEntityInfo(Entity entity,
				Collection<? extends Entity> visibleSet, Config config) {
			this.entity = entity;
			this.visibleSet = visibleSet;
			this.config = config;
		}

		@Override
		public String toString() {
			return entity.toString();
		}
	}
}
