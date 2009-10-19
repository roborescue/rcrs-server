package kernel;

import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.io.IOException;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.Constants;

import rescuecore2.log.LogWriter;
import rescuecore2.log.FileLogWriter;
import rescuecore2.log.InitialConditionsRecord;
import rescuecore2.log.StartLogRecord;
import rescuecore2.log.EndLogRecord;
import rescuecore2.log.ConfigRecord;
import rescuecore2.log.PerceptionRecord;
import rescuecore2.log.CommandsRecord;
import rescuecore2.log.UpdatesRecord;
import rescuecore2.log.LogException;

/**
   The Robocup Rescue kernel.
 */
public class Kernel {
    private static final String AGENT_TIME_KEY = "kernel.agents.think-time";

    //    private Config config;
    private Perception perception;
    private CommunicationModel communicationModel;
    private WorldModel<? extends Entity> worldModel;
    private LogWriter log;

    private Set<KernelListener> listeners;

    private Collection<AgentProxy> agents;
    private Collection<SimulatorProxy> sims;
    private Collection<ViewerProxy> viewers;
    private int time;
    private Collection<Command> agentCommandsLastTimestep;

    private CommandFilter commandFilter;

    private TerminationCondition termination;

    private int agentTime;

    /**
       Construct a kernel.
       @param config The configuration to use.
       @param perception A perception calculator.
       @param communicationModel A communication model.
       @param worldModel The world model.
       @param commandFilter An optional command filter. This may be null.
       @param termination The termination condition.
       @throws KernelException If there is a problem constructing the kernel.
    */
    public Kernel(Config config,
                  Perception perception,
                  CommunicationModel communicationModel,
                  WorldModel<? extends Entity> worldModel,
                  CommandFilter commandFilter,
                  TerminationCondition termination) throws KernelException {
        //        this.config = config;
        this.perception = perception;
        this.communicationModel = communicationModel;
        this.worldModel = worldModel;
        this.commandFilter = commandFilter;
        listeners = new HashSet<KernelListener>();
        agents = new HashSet<AgentProxy>();
        sims = new HashSet<SimulatorProxy>();
        viewers = new HashSet<ViewerProxy>();
        time = 0;
        agentTime = config.getIntValue(AGENT_TIME_KEY);
        agentCommandsLastTimestep = new HashSet<Command>();
        try {
            String logName = config.getValue("kernel.logname");
            System.out.println("Logging to " + logName);
            log = new FileLogWriter(logName);
            log.writeRecord(new StartLogRecord());
            log.writeRecord(new InitialConditionsRecord(worldModel));
            log.writeRecord(new ConfigRecord(config));
        }
        catch (IOException e) {
            throw new KernelException("Couldn't open log file for writing", e);
        }
        catch (LogException e) {
            throw new KernelException("Couldn't open log file for writing", e);
        }
        commandFilter.initialise(config, this);
        this.termination = termination;
        config.setValue(Constants.COMMUNICATION_MODEL_KEY, communicationModel.getClass().getName());
        config.setValue(Constants.PERCEPTION_KEY, perception.getClass().getName());
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
    public void addAgent(AgentProxy agent) {
        synchronized (this) {
            agents.add(agent);
        }
        fireAgentAdded(agent);
    }

    /**
       Remove an agent from the system.
       @param agent The agent to remove.
    */
    public void removeAgent(AgentProxy agent) {
        synchronized (this) {
            agents.remove(agent);
        }
        fireAgentRemoved(agent);
    }

    /**
       Get all agents in the system.
       @return An unmodifiable view of all agents.
    */
    public Collection<AgentProxy> getAllAgents() {
        synchronized (this) {
            return Collections.unmodifiableCollection(agents);
        }
    }

    /**
       Add a simulator to the system.
       @param sim The simulator to add.
    */
    public void addSimulator(SimulatorProxy sim) {
        synchronized (this) {
            sims.add(sim);
        }
        fireSimulatorAdded(sim);
    }

    /**
       Remove a simulator from the system.
       @param sim The simulator to remove.
    */
    public void removeSimulator(SimulatorProxy sim) {
        synchronized (this) {
            sims.remove(sim);
        }
        fireSimulatorRemoved(sim);
    }

    /**
       Get all simulators in the system.
       @return An unmodifiable view of all simulators.
    */
    public Collection<SimulatorProxy> getAllSimulators() {
        synchronized (this) {
            return Collections.unmodifiableCollection(sims);
        }
    }

    /**
       Add a viewer to the system.
       @param viewer The viewer to add.
    */
    public void addViewer(ViewerProxy viewer) {
        synchronized (this) {
            viewers.add(viewer);
        }
        fireViewerAdded(viewer);
    }

    /**
       Remove a viewer from the system.
       @param viewer The viewer to remove.
    */
    public void removeViewer(ViewerProxy viewer) {
        synchronized (this) {
            viewers.remove(viewer);
        }
        fireViewerRemoved(viewer);
    }

    /**
       Get all viewers in the system.
       @return An unmodifiable view of all viewers.
    */
    public Collection<ViewerProxy> getAllViewers() {
        synchronized (this) {
            return Collections.unmodifiableCollection(viewers);
        }
    }

    /**
       Run a single timestep.
       @throws InterruptedException If this thread is interrupted during the timestep.
       @throws KernelException If there is a problem executing the timestep.
       @throws LogException If there is a problem writing the log.
    */
    public void timestep() throws InterruptedException, KernelException, LogException {
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
            log.writeRecord(new CommandsRecord(time, agentCommandsLastTimestep));
            long commandsTime = System.currentTimeMillis();
            System.out.println("Broadcasting commands");
            Collection<Entity> updates = sendCommandsToViewersAndSimulators(time, agentCommandsLastTimestep);
            log.writeRecord(new UpdatesRecord(time, updates));
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
    public WorldModel<? extends Entity> getWorldModel() {
        return worldModel;
    }

    /**
       Shut down the kernel. This method will notify all agents/simulators/viewers of the shutdown.
    */
    public void shutdown() {
        synchronized (this) {
            for (AgentProxy next : agents) {
                next.shutdown();
            }
            for (SimulatorProxy next : sims) {
                next.shutdown();
            }
            for (ViewerProxy next : viewers) {
                next.shutdown();
            }
            try {
                log.writeRecord(new EndLogRecord());
                log.close();
            }
            catch (LogException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Kernel has shut down");
    }

    /**
       Set the amount of time the kernel will wait for agent commands, in milliseconds.
       @param newWaitTime The new wait time.
    */
    public void setAgentWaitTime(int newWaitTime) {
        synchronized (this) {
            agentTime = newWaitTime;
        }
    }

    /**
       Find out if the kernel has terminated.
       @return True if the kernel has terminated, false otherwise.
    */
    public boolean hasTerminated() {
        synchronized (this) {
            return termination.shouldStop(this);
        }
    }

    private void sendAgentUpdates(int timestep, Collection<Command> commandsLastTimestep) throws InterruptedException, KernelException, LogException {
        perception.setTime(timestep);
        Map<AgentProxy, Collection<Message>> comms = communicationModel.process(timestep, agents, commandsLastTimestep);
        for (AgentProxy next : agents) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Collection<Entity> visible = perception.getVisibleEntities(next);
            log.writeRecord(new PerceptionRecord(timestep, next.getControlledEntity().getID(), visible, comms.get(next)));
            next.sendPerceptionUpdate(timestep, visible, comms.get(next));
        }
    }

    private Collection<Command> waitForCommands(int timestep) throws InterruptedException {
        long now = System.currentTimeMillis();
        long end = now + agentTime;
        while (now < end) {
            Thread.sleep(end - now);
            now = System.currentTimeMillis();
        }
        Collection<Command> result = new HashSet<Command>();
        for (AgentProxy next : agents) {
            Collection<Command> commands = next.getAgentCommands(timestep);
            if (commandFilter != null) {
                commandFilter.filter(commands, next);
            }
            result.addAll(commands);
        }
        return result;
    }

    /**
       Send commands to all viewers and simulators and return which entities have been updated by the simulators.
    */
    private Collection<Entity> sendCommandsToViewersAndSimulators(int timestep, Collection<Command> commands) throws InterruptedException {
        for (SimulatorProxy next : sims) {
            next.sendAgentCommands(timestep, commands);
        }
        for (ViewerProxy next : viewers) {
            next.sendAgentCommands(timestep, commands);
        }
        // Wait until all simulators have sent updates
        Collection<Entity> result = new HashSet<Entity>();
        for (SimulatorProxy next : sims) {
            result.addAll(next.getUpdates(timestep));
        }
        return result;
    }

    private void sendUpdatesToViewersAndSimulators(int timestep, Collection<Entity> updates) throws InterruptedException {
        for (SimulatorProxy next : sims) {
            next.sendUpdate(timestep, updates);
        }
        for (ViewerProxy next : viewers) {
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

    private void fireAgentAdded(AgentProxy agent) {
        for (KernelListener next : getListeners()) {
            next.agentAdded(agent);
        }
    }

    private void fireAgentRemoved(AgentProxy agent) {
        for (KernelListener next : getListeners()) {
            next.agentRemoved(agent);
        }
    }

    private void fireSimulatorAdded(SimulatorProxy sim) {
        for (KernelListener next : getListeners()) {
            next.simulatorAdded(sim);
        }
    }

    private void fireSimulatorRemoved(SimulatorProxy sim) {
        for (KernelListener next : getListeners()) {
            next.simulatorRemoved(sim);
        }
    }

    private void fireViewerAdded(ViewerProxy viewer) {
        for (KernelListener next : getListeners()) {
            next.viewerAdded(viewer);
        }
    }

    private void fireViewerRemoved(ViewerProxy viewer) {
        for (KernelListener next : getListeners()) {
            next.viewerRemoved(viewer);
        }
    }
}