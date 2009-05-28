package kernel;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionManager;
import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;

import rescuecore2.version0.messages.Version0MessageFactory;

/**
   The Robocup Rescue kernel.
   @param <S> The subclass of WorldModel that this kernel operates on.
   @param <T> The subclass of Entity that this kernel operates on.
 */
public class Kernel<T extends Entity, S extends WorldModel<T>> {
    private Config config;
    private WorldModelCreator<T, S> worldModelCreator;
    private SimulatorManager<T, S> simulatorManager;
    private ViewerManager<T, S> viewerManager;
    private AgentManager<T, S> agentManager;
    private Perception<T, S> perception;
    private CommunicationModel<T, S> communicationModel;

    private S worldModel;

    private ConnectionManager connectionManager;

    private Set<KernelListener> listeners;

    private Collection<Agent<T>> agents;
    private Collection<Simulator<T>> sims;
    private Collection<Viewer<T>> viewers;

    /**
       Construct a kernel.
       @param config The configuration to use.
       @param worldModelCreator An object that will create the world model.
       @param simulatorManager A manager for simulators.
       @param viewerManager A manager for viewers.
       @param agentManager A manager for agents.
       @param perception A perception calculator.
       @param communicationModel A communication model.
       @throws KernelException If something blows up.
       @throws ConfigException If the config file is broken.
    */
    public Kernel(Config config,
                  WorldModelCreator<T, S> worldModelCreator,
                  SimulatorManager<T, S> simulatorManager,
                  ViewerManager<T, S> viewerManager,
                  AgentManager<T, S> agentManager,
                  Perception<T, S> perception,
                  CommunicationModel<T, S> communicationModel) throws KernelException, ConfigException {
        this.config = config;
        this.worldModelCreator = worldModelCreator;
        this.simulatorManager = simulatorManager;
        this.viewerManager = viewerManager;
        this.agentManager = agentManager;
        this.perception = perception;
        this.communicationModel = communicationModel;
        listeners = new HashSet<KernelListener>();
        agents = new HashSet<Agent<T>>();
        sims = new HashSet<Simulator<T>>();
        viewers = new HashSet<Viewer<T>>();
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
       Start the kernel, run the simulation and clean up.
       @throws KernelException If there is a problem running the simulation.
       @throws InterruptedException If the simulation is interrupted.
    */
    public void runSimulation() throws KernelException, InterruptedException {
        buildWorldModel();
        setupCommunication();
        waitForSimulatorsAndAgents();
        setupSimulation();
        waitForSimulationToFinish();
        cleanUp();
    }

    private void buildWorldModel() throws KernelException {
        worldModel = worldModelCreator.buildWorldModel(config);
        simulatorManager.setWorldModel(worldModel);
        viewerManager.setWorldModel(worldModel);
        agentManager.setWorldModel(worldModel);
        perception.setWorldModel(worldModel);
        communicationModel.setWorldModel(worldModel);
        fireWorldModelComplete();
    }

    private void setupCommunication() throws KernelException {
        connectionManager = new ConnectionManager();
        ConnectionManagerListener listener = new ConnectionManagerListener() {
                public void newConnection(Connection c) {
                    System.out.println("New connection: " + c);
                    simulatorManager.newConnection(c);
                    viewerManager.newConnection(c);
                    agentManager.newConnection(c);
                    c.startup();
                }
            };
        try {
            connectionManager.listen(config.getIntValue("kernel_port"), Version0MessageFactory.INSTANCE, listener);
        }
        catch (IOException e) {
            throw new KernelException("Couldn't open kernel port", e);
        }
    }

    private void waitForSimulatorsAndAgents() throws KernelException, InterruptedException {
        agents = agentManager.getAllAgents();
        sims = simulatorManager.getAllSimulators();
        viewers = viewerManager.getAllViewers();
    }

    private void setupSimulation() {
    }

    private void waitForSimulationToFinish() throws InterruptedException {
        int timestep = 0;
        int maxTimestep = config.getIntValue("timesteps");
        Collection<Command> agentCommands = new HashSet<Command>();
        // Each timestep:
        // Work out what the agents can see and hear (using the commands from the previous timestep).
        // Wait for new commands
        // Send commands to simulators/viewers and wait for updates
        // Collate updates and broadcast to simulators/viewers
        while (timestep < maxTimestep) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            ++timestep;
            fireTimestep(timestep);
            System.out.println("Timestep " + timestep);
            sendAgentUpdates(timestep, agentCommands);
            agentCommands = waitForCommands(timestep);
            Collection<T> updates = sendCommandsToViewersAndSimulators(timestep, agentCommands);
            // Merge updates into world model
            worldModel.merge(updates);
            sendUpdatesToViewersAndSimulators(timestep, updates);
        }
    }

    private void cleanUp() {
        connectionManager.shutdown();
        simulatorManager.shutdown();
        viewerManager.shutdown();
        agentManager.shutdown();
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
            next.sendPerceptionUpdate(timestep, visible);
            Collection<Message> comms = communicationModel.process(next, commandsLastTimestep);
            next.sendMessages(comms);
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

    private Set<KernelListener> getListeners() {
        Set<KernelListener> result;
        synchronized (listeners) {
            result = new HashSet<KernelListener>(listeners);
        }
        return result;
    }

    private void fireWorldModelComplete() {
        for (KernelListener next : getListeners()) {
            next.worldModelComplete();
        }
    }

    private void fireTimestep(int time) {
        for (KernelListener next : getListeners()) {
            next.timestep(time);
        }
    }
}