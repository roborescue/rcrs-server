package kernel;

import java.io.IOException;
import java.util.Collection;

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
   @param <T> The subclass of Entity that this kernel operates on.
 */
public class Kernel<T extends Entity> {
    private Config config;
    private WorldModelCreator<T> worldModelCreator;
    private SimulatorManager<T> simulatorManager;
    private ViewerManager<T> viewerManager;
    private AgentManager<T> agentManager;
    private Perception<T> perception;

    private WorldModel<T> worldModel;

    private ConnectionManager connectionManager;

    /**
       Construct a kernel.
       @param config The configuration to use.
       @param worldModelCreator An object that will create the world model.
       @param simulatorManager A manager for simulators.
       @param viewerManager A manager for viewers.
       @param agentManager A manager for agents.
       @param perception A perception calculator.
       @throws KernelException If something blows up.
       @throws ConfigException If the config file is broken.
    */
    public Kernel(Config config,
                  WorldModelCreator<T> worldModelCreator,
                  SimulatorManager<T> simulatorManager,
                  ViewerManager<T> viewerManager,
                  AgentManager<T> agentManager,
                  Perception<T> perception) throws KernelException, ConfigException {
        this.config = config;
        this.worldModelCreator = worldModelCreator;
        this.simulatorManager = simulatorManager;
        this.viewerManager = viewerManager;
        this.agentManager = agentManager;
        this.perception = perception;
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
        while (timestep < maxTimestep) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            ++timestep;
            System.out.println("Timestep " + timestep);
            sendAgentUpdates(timestep);
            waitForCommands();
            sendCommandsToViewersAndSimulators(timestep);
            sendUpdatesToViewersAndSimulators(timestep);
        }
    }

    private void cleanUp() {
        connectionManager.shutdown();
        simulatorManager.shutdown();
        viewerManager.shutdown();
        agentManager.shutdown();
    }

    private void sendAgentUpdates(int timestep) throws InterruptedException {
        for (T next : agentManager.getControlledEntities()) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Collection<T> visible = perception.getVisibleEntities(next);
            agentManager.sendPerceptionUpdate(timestep, next, visible);
        }
    }

    private void waitForCommands() throws InterruptedException {
        long now = System.currentTimeMillis();
        long end = now + config.getIntValue("step");
        while (now < end) {
            Thread.sleep(end - now);
            now = System.currentTimeMillis();
        }
    }

    private void sendCommandsToViewersAndSimulators(int timestep) {
        Collection<Message> commands = agentManager.getAgentCommands(timestep);
        simulatorManager.sendToAll(commands);
        viewerManager.sendToAll(commands);
    }

    private void sendUpdatesToViewersAndSimulators(int time) throws InterruptedException {
        // Wait until all simulators have sent updates
        Collection<T> updates = simulatorManager.getAllUpdates();
        // Merge into world model
        worldModel.merge(updates);
        // Send updates
        simulatorManager.sendUpdate(time, updates);
        viewerManager.sendUpdate(time, updates);
    }
}