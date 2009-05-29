package kernel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;

/**
   The Robocup Rescue kernel.
   @param <T> The subclass of Entity that this kernel operates on.
 */
public class Kernel<T extends Entity> {
    private Config config;
    private Perception<T> perception;
    private CommunicationModel<T> communicationModel;
    private WorldModel<T> worldModel;

    private Set<KernelListener<T>> listeners;

    private Collection<Agent<T>> agents;
    private Collection<Simulator<T>> sims;
    private Collection<Viewer<T>> viewers;
    private int time;
    private Collection<Command> agentCommandsLastTimestep;

    /**
       Construct a kernel.
       @param config The configuration to use.
       @param perception A perception calculator.
       @param communicationModel A communication model.
       @param worldModel The world model.
       @throws KernelException If something blows up.
       @throws ConfigException If the config file is broken.
    */
    public Kernel(Config config,
                  Perception<T> perception,
                  CommunicationModel<T> communicationModel,
                  WorldModel<T> worldModel) throws KernelException, ConfigException {
        this.config = config;
        this.perception = perception;
        this.communicationModel = communicationModel;
        this.worldModel = worldModel;
        listeners = new HashSet<KernelListener<T>>();
        agents = new HashSet<Agent<T>>();
        sims = new HashSet<Simulator<T>>();
        viewers = new HashSet<Viewer<T>>();
        time = 0;
        agentCommandsLastTimestep = new HashSet<Command>();
    }

    /**
       Add a KernelListener.
       @param l The listener to add.
    */
    public void addKernelListener(KernelListener<T> l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    /**
       Remove a KernelListener.
       @param l The listener to remove.
    */
    public void removeKernelListener(KernelListener<T> l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
       Add an agent to the system.
       @param agent The agent to add.
    */
    public void addAgent(Agent<T> agent) {
        synchronized (this) {
            agents.add(agent);
        }
        fireAgentAdded(agent);
    }

    /**
       Remove an agent from the system.
       @param agent The agent to remove.
    */
    public void removeAgent(Agent<T> agent) {
        synchronized (this) {
            agents.remove(agent);
        }
        fireAgentRemoved(agent);
    }

    /**
       Add a simulator to the system.
       @param sim The simulator to add.
    */
    public void addSimulator(Simulator<T> sim) {
        synchronized (this) {
            sims.add(sim);
        }
        fireSimulatorAdded(sim);
    }

    /**
       Remove a simulator from the system.
       @param sim The simulator to remove.
    */
    public void removeSimulator(Simulator<T> sim) {
        synchronized (this) {
            sims.remove(sim);
        }
        fireSimulatorRemoved(sim);
    }

    /**
       Add a viewer to the system.
       @param viewer The viewer to add.
    */
    public void addViewer(Viewer<T> viewer) {
        synchronized (this) {
            viewers.add(viewer);
        }
        fireViewerAdded(viewer);
    }

    /**
       Remove a viewer from the system.
       @param viewer The viewer to remove.
    */
    public void removeViewer(Viewer<T> viewer) {
        synchronized (this) {
            viewers.remove(viewer);
        }
        fireViewerRemoved(viewer);
    }

    /**
       Run a single timestep.
       @throws InterruptedException If this thread is interrupted during the timestep.
    */
    public void timestep() throws InterruptedException {
        synchronized (this) {
            ++time;
            // Work out what the agents can see and hear (using the commands from the previous timestep).
            // Wait for new commands
            // Send commands to simulators/viewers and wait for updates
            // Collate updates and broadcast to simulators/viewers
            System.out.println("Timestep " + time);
            System.out.println("Sending agent updates");
            sendAgentUpdates(time, agentCommandsLastTimestep);
            System.out.println("Waiting for commands");
            agentCommandsLastTimestep = waitForCommands(time);
            System.out.println("Broadcasting commands");
            Collection<T> updates = sendCommandsToViewersAndSimulators(time, agentCommandsLastTimestep);
            // Merge updates into world model
            System.out.println("Broadcasting updates");
            worldModel.merge(updates);
            sendUpdatesToViewersAndSimulators(time, updates);
            System.out.println("Timestep " + time + " complete");
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
    public WorldModel<T> getWorldModel() {
        return worldModel;
    }

    /**
       Shut down the kernel. This method will notify all agents/simulators/viewers of the shutdown.
     */
    public void shutdown() {
        for (Agent<T> next : agents) {
            next.shutdown();
        }
        for (Simulator<T> next : sims) {
            next.shutdown();
        }
        for (Viewer<T> next : viewers) {
            next.shutdown();
        }
    }

    private void sendAgentUpdates(int timestep, Collection<Command> commandsLastTimestep) throws InterruptedException {
        perception.setTime(timestep);
        for (Agent<T> next : agents) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Collection<T> visible = perception.getVisibleEntities(next);
            Collection<Message> comms = communicationModel.process(next, commandsLastTimestep);
            next.sendPerceptionUpdate(timestep, visible, comms);
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
        for (Agent<T> next : agents) {
            result.addAll(next.getAgentCommands(timestep));
        }
        return result;
    }

    /**
       Send commands to all viewers and simulators and return which entities have been updated by the simulators.
    */
    private Collection<T> sendCommandsToViewersAndSimulators(int timestep, Collection<Command> commands) throws InterruptedException {
        for (Simulator<T> next : sims) {
            next.sendAgentCommands(timestep, commands);
        }
        for (Viewer<T> next : viewers) {
            next.sendAgentCommands(timestep, commands);
        }
        // Wait until all simulators have sent updates
        Collection<T> result = new HashSet<T>();
        for (Simulator<T> next : sims) {
            result.addAll(next.getUpdates(timestep));
        }
        return result;
    }

    private void sendUpdatesToViewersAndSimulators(int timestep, Collection<T> updates) throws InterruptedException {
        for (Simulator<T> next : sims) {
            next.sendUpdate(timestep, updates);
        }
        for (Viewer<T> next : viewers) {
            next.sendUpdate(timestep, updates);
        }
    }

    private Set<KernelListener<T>> getListeners() {
        Set<KernelListener<T>> result;
        synchronized (listeners) {
            result = new HashSet<KernelListener<T>>(listeners);
        }
        return result;
    }

    private void fireTimestepCompleted(int timestep) {
        for (KernelListener<T> next : getListeners()) {
            next.timestepCompleted(timestep);
        }
    }

    private void fireAgentAdded(Agent<T> agent) {
        for (KernelListener<T> next : getListeners()) {
            next.agentAdded(agent);
        }
    }

    private void fireAgentRemoved(Agent<T> agent) {
        for (KernelListener<T> next : getListeners()) {
            next.agentRemoved(agent);
        }
    }

    private void fireSimulatorAdded(Simulator<T> sim) {
        for (KernelListener<T> next : getListeners()) {
            next.simulatorAdded(sim);
        }
    }

    private void fireSimulatorRemoved(Simulator<T> sim) {
        for (KernelListener<T> next : getListeners()) {
            next.simulatorRemoved(sim);
        }
    }

    private void fireViewerAdded(Viewer<T> viewer) {
        for (KernelListener<T> next : getListeners()) {
            next.viewerAdded(viewer);
        }
    }

    private void fireViewerRemoved(Viewer<T> viewer) {
        for (KernelListener<T> next : getListeners()) {
            next.viewerRemoved(viewer);
        }
    }
}