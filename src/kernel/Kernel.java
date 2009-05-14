package kernel;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionManager;
import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.messages.Message;

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
        agentManager.waitForAllAgents();
        viewerManager.waitForAcknowledgements();
        simulatorManager.waitForAcknowledgements();
    }

    private void setupSimulation() {
    }

    private void waitForSimulationToFinish() throws InterruptedException {
        int timestep = 0;
        int maxTimestep = config.getIntValue("timesteps");
        Collection<Message> agentCommands = new HashSet<Message>();
        Collection<T> updates = new HashSet<T>();
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
            System.out.println("Timestep " + timestep);
            sendAgentUpdates(timestep, agentCommands);
            agentCommands = waitForCommands(timestep);
            updates = sendCommandsToViewersAndSimulators(timestep, agentCommands);
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
    }

    private void sendAgentUpdates(int timestep, Collection<Message> commandsLastTimestep) throws InterruptedException {
        for (T next : agentManager.getControlledEntities()) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Collection<T> visible = perception.getVisibleEntities(next);
            agentManager.sendPerceptionUpdate(timestep, next, visible);
            Collection<Message> comms = communicationModel.process(next, commandsLastTimestep);
            agentManager.sendMessages(next, comms);
        }
    }

    private Collection<Message> waitForCommands(int timestep) throws InterruptedException {
        long now = System.currentTimeMillis();
        long end = now + config.getIntValue("step");
        while (now < end) {
            Thread.sleep(end - now);
            now = System.currentTimeMillis();
        }
        return agentManager.getAgentCommands(timestep);
    }

    private Collection<T> sendCommandsToViewersAndSimulators(int timestep, Collection<Message> commands) throws InterruptedException {
        simulatorManager.sendAgentCommands(timestep, commands);
        viewerManager.sendAgentCommands(timestep, commands);
        // Wait until all simulators have sent updates
        return simulatorManager.getAllUpdates();
    }

    private void sendUpdatesToViewersAndSimulators(int timestep, Collection<T> updates) throws InterruptedException {
        simulatorManager.sendUpdate(timestep, updates);
        viewerManager.sendUpdate(timestep, updates);
    }
}