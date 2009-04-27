package kernel;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.connection.TCPConnection;
import rescuecore2.connection.ConnectionManager;
import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.MessageFactory;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.version0.messages.Version0MessageFactory;

import kernel.legacy.LegacyAgentManager;
import kernel.legacy.LegacyWorldModelCreator;

/**
   The Robocup Rescue kernel.
 */
public class Kernel {
    private static final String CONFIG_FLAG = "-c";
    private static final String CONFIG_LONG_FLAG = "--config";

    private Config config;
    private WorldModelCreator worldModelCreator;

    private ConnectionManager connectionManager;
    private AgentManager agentManager;

    private WorldModel worldModel;

    /**
       Construct a kernel from some command line arguments.
       @param args The command line arguments.
       @throws KernelException If something blows up.
       @throws ConfigException If the config file is broken.
    */
    public Kernel(String[] args) throws KernelException, ConfigException {
        config = new Config();
        int i = 0;
        while (i < args.length) {
            if (args[i].equalsIgnoreCase(CONFIG_FLAG) || args[i].equalsIgnoreCase(CONFIG_LONG_FLAG)) {
                try {
                    config.read(new File(args[++i]));
                }
                catch (IOException e) {
                    throw new KernelException("Error reading config file", e);
                }
            }
            else {
                System.out.println("Unrecognised option: " + args[i]);
            }
            ++i;
        }
    }

    /**
       Start the kernel!
       @param args The command line arguments.
    */
    public static void main(String[] args) {
        try {
            new Kernel(args).runSimulation();
        }
        catch (KernelException e) {
            e.printStackTrace();
        }
        catch (ConfigException e) {
            e.printStackTrace();
        }
    }

    /**
       Start the kernel, run the simulation and clean up.
       @throws KernelException If there is a problem running the simulation.
    */
    public void runSimulation() throws KernelException {
        buildWorldModel();
        openSockets();
        waitForSimulatorsAndAgents();
        waitForSimulationToFinish();
        cleanUp();
    }

    private void buildWorldModel() throws KernelException {
        worldModelCreator = new LegacyWorldModelCreator();
        worldModel = worldModelCreator.buildWorldModel(config);
    }

    private void openSockets() throws KernelException {
        connectionManager = new ConnectionManager();
        ConnectionManagerListener listener = new ConnectionManagerListener() {
                public void newConnection(Connection c) {
                    System.out.println("New connection: " + c);
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

    private void waitForSimulatorsAndAgents() throws KernelException {
        agentManager = new LegacyAgentManager(worldModel);
        try {
            agentManager.waitForAllAgents();
        }
        catch (InterruptedException e) {
            throw new KernelException("Interrupted while waiting for agents", e);
        }
    }

    private void waitForSimulationToFinish() {
    }

    private void cleanUp() {
        connectionManager.shutdown();
    }
}