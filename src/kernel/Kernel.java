package kernel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;

import kernel.log.LogWriter;
import kernel.log.FileLogWriter;

/**
   The Robocup Rescue kernel.
 */
public class Kernel {
    private Config config;
    private Perception perception;
    private CommunicationModel communicationModel;
    private WorldModel<? extends Entity> worldModel;
    private LogWriter log;

    private Set<KernelListener> listeners;

    private Collection<Agent> agents;
    private Collection<Simulator> sims;
    private Collection<Viewer> viewers;
    private int time;
    private int freezeTime;
    private Collection<Command> agentCommandsLastTimestep;

    /**
       Construct a kernel.
       @param config The configuration to use.
       @param perception A perception calculator.
       @param communicationModel A communication model.
       @param worldModel The world model.
       @throws KernelException If there is a problem constructing the kernel.
    */
    public Kernel(Config config,
                  Perception perception,
                  CommunicationModel communicationModel,
                  WorldModel<? extends Entity> worldModel) throws KernelException {
        this.config = config;
        this.perception = perception;
        this.communicationModel = communicationModel;
        this.worldModel = worldModel;
        listeners = new HashSet<KernelListener>();
        agents = new HashSet<Agent>();
        sims = new HashSet<Simulator>();
        viewers = new HashSet<Viewer>();
        time = 0;
        freezeTime = config.getIntValue("steps_agents_frozen", 0);
        agentCommandsLastTimestep = new HashSet<Command>();
        log = new FileLogWriter(config);
        log.logInitialConditions(worldModel);
    }

    /**
       Add a KernelListener.
       @param l The listener to add.
    */
    public void addKernelListener(KernelListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    /**
       Remove a KernelListener.
       @param l The listener to remove.
    */
    public void removeKernelListener(KernelListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
       Add an agent to the system.
       @param agent The agent to add.
    */
    public void addAgent(Agent agent) {
        synchronized (this) {
            agents.add(agent);
        }
        fireAgentAdded(agent);
    }

    /**
       Remove an agent from the system.
       @param agent The agent to remove.
    */
    public void removeAgent(Agent agent) {
        synchronized (this) {
            agents.remove(agent);
        }
        fireAgentRemoved(agent);
    }

    /**
       Add a simulator to the system.
       @param sim The simulator to add.
    */
    public void addSimulator(Simulator sim) {
        synchronized (this) {
            sims.add(sim);
        }
        fireSimulatorAdded(sim);
    }

    /**
       Remove a simulator from the system.
       @param sim The simulator to remove.
    */
    public void removeSimulator(Simulator sim) {
        synchronized (this) {
            sims.remove(sim);
        }
        fireSimulatorRemoved(sim);
    }

    /**
       Add a viewer to the system.
       @param viewer The viewer to add.
    */
    public void addViewer(Viewer viewer) {
        synchronized (this) {
            viewers.add(viewer);
        }
        fireViewerAdded(viewer);
    }

    /**
       Remove a viewer from the system.
       @param viewer The viewer to remove.
    */
    public void removeViewer(Viewer viewer) {
        synchronized (this) {
            viewers.remove(viewer);
        }
        fireViewerRemoved(viewer);
    }

    /**
       Run a single timestep.
       @throws InterruptedException If this thread is interrupted during the timestep.
       @throws KernelException If there is a problem executing the timestep.
    */
    public void timestep() throws InterruptedException, KernelException {
        synchronized (this) {
            ++time;
            // Work out what the agents can see and hear (using the commands from the previous timestep).
            // Wait for new commands
            // Send commands to simulators/viewers and wait for updates
            // Collate updates and broadcast to simulators/viewers
            System.out.println("Timestep " + time);
            System.out.println("Sending agent updates");
            long start = System.currentTimeMillis();
            sendAgentUpdates(time, agentCommandsLastTimestep);
            long perceptionTime = System.currentTimeMillis();
            System.out.println("Waiting for commands");
            agentCommandsLastTimestep = waitForCommands(time);
            log.logCommands(time, agentCommandsLastTimestep);
            long commandsTime = System.currentTimeMillis();
            System.out.println("Broadcasting commands");
            Collection<Entity> updates = sendCommandsToViewersAndSimulators(time, agentCommandsLastTimestep);
            log.logUpdates(time, updates);
            long updatesTime = System.currentTimeMillis();
            // Merge updates into world model
            System.out.println("Broadcasting updates");
            worldModel.merge(updates);
            long mergeTime = System.currentTimeMillis();
            sendUpdatesToViewersAndSimulators(time, updates);
            long broadcastTime = System.currentTimeMillis();
            System.out.println("Timestep " + time + " complete");
            System.out.println("Perception took        : " + (perceptionTime - start) + "ms");
            System.out.println("Agent commands took    : " + (commandsTime - perceptionTime) + "ms");
            System.out.println("Simulator updates took : " + (updatesTime - commandsTime) + "ms");
            System.out.println("World model merge took : " + (mergeTime - updatesTime) + "ms");
            System.out.println("Update broadcast took  : " + (broadcastTime - mergeTime) + "ms");
            System.out.println("Total time             : " + (broadcastTime - start) + "ms");
            fireTimestepCompleted(time);
        }
    }

    /**
       Get the current time.
       @return The current time.
     */
    public int getTime() {
        synchronized (this) {
            return time;
        }
    }

    /**
       Get the world model.
       @return The world model.
    */
    public WorldModel getWorldModel() {
        return worldModel;
    }

    /**
       Shut down the kernel. This method will notify all agents/simulators/viewers of the shutdown.
     */
    public void shutdown() {
        for (Agent next : agents) {
            next.shutdown();
        }
        for (Simulator next : sims) {
            next.shutdown();
        }
        for (Viewer next : viewers) {
            next.shutdown();
        }
        log.close();
    }

    private void sendAgentUpdates(int timestep, Collection<Command> commandsLastTimestep) throws InterruptedException, KernelException {
        perception.setTime(timestep);
        communicationModel.setTime(timestep);
        Map<Agent, Collection<Message>> comms = communicationModel.process(agents, commandsLastTimestep);
        for (Agent next : agents) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Collection<Entity> visible = perception.getVisibleEntities(next);
            log.logPerception(timestep, next.getControlledEntity().getID(), visible, comms.get(next));
            next.sendPerceptionUpdate(timestep, visible, comms.get(next));
        }
    }

    private Collection<Command> waitForCommands(int timestep) throws InterruptedException {
        long now = System.currentTimeMillis();
        long end = now + config.getIntValue("step");
        while (now < end) {
            Thread.sleep(end - now);
            now = System.currentTimeMillis();
        }
        Collection<Command> result = new HashSet<Command>();
        if (timestep >= freezeTime) {
            for (Agent next : agents) {
                result.addAll(next.getAgentCommands(timestep));
            }
        }
        return result;
    }

    /**
       Send commands to all viewers and simulators and return which entities have been updated by the simulators.
    */
    private Collection<Entity> sendCommandsToViewersAndSimulators(int timestep, Collection<Command> commands) throws InterruptedException {
        for (Simulator next : sims) {
            next.sendAgentCommands(timestep, commands);
        }
        for (Viewer next : viewers) {
            next.sendAgentCommands(timestep, commands);
        }
        // Wait until all simulators have sent updates
        Collection<Entity> result = new HashSet<Entity>();
        for (Simulator next : sims) {
            result.addAll(next.getUpdates(timestep));
        }
        return result;
    }

    private void sendUpdatesToViewersAndSimulators(int timestep, Collection<Entity> updates) throws InterruptedException {
        for (Simulator next : sims) {
            next.sendUpdate(timestep, updates);
        }
        for (Viewer next : viewers) {
            next.sendUpdate(timestep, updates);
        }
    }

    private Set<KernelListener> getListeners() {
        Set<KernelListener> result;
        synchronized (listeners) {
            result = new HashSet<KernelListener>(listeners);
        }
        return result;
    }

    private void fireTimestepCompleted(int timestep) {
        for (KernelListener next : getListeners()) {
            next.timestepCompleted(timestep);
        }
    }

    private void fireAgentAdded(Agent agent) {
        for (KernelListener next : getListeners()) {
            next.agentAdded(agent);
        }
    }

    private void fireAgentRemoved(Agent agent) {
        for (KernelListener next : getListeners()) {
            next.agentRemoved(agent);
        }
    }

    private void fireSimulatorAdded(Simulator sim) {
        for (KernelListener next : getListeners()) {
            next.simulatorAdded(sim);
        }
    }

    private void fireSimulatorRemoved(Simulator sim) {
        for (KernelListener next : getListeners()) {
            next.simulatorRemoved(sim);
        }
    }

    private void fireViewerAdded(Viewer viewer) {
        for (KernelListener next : getListeners()) {
            next.viewerAdded(viewer);
        }
    }

    private void fireViewerRemoved(Viewer viewer) {
        for (KernelListener next : getListeners()) {
            next.viewerRemoved(viewer);
        }
    }
}