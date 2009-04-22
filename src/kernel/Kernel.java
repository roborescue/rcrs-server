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
import rescuecore2.messages.MessageCodec;
import rescuecore2.messages.Message;
import rescuecore2.messages.legacy.LegacyMessageCodec;
import rescuecore2.messages.legacy.KGConnect;
import rescuecore2.messages.legacy.KGAcknowledge;
import rescuecore2.messages.legacy.GKConnectOK;
import rescuecore2.messages.legacy.GKConnectError;
import rescuecore2.worldmodel.WorldModel;

import kernel.legacy.LegacyAgentManager;

/**
   The Robocup Rescue kernel.
 */
public class Kernel {
    private static final String CONFIG_FLAG = "-c";
    private static final String CONFIG_LONG_FLAG = "--config";

    private Config config;
    private MessageCodec codec;
    private Connection gisConnection;

    private ConnectionManager connectionManager;
    private AgentManager agentManager;

    private WorldModel worldModel;

    /**
       Construct a kernel from some command line arguments.
       @param args The command line arguments.
       @throws IOException If there is a problem reading the config file.
       @throws ConfigException If the config file is broken.
    */
    public Kernel(String[] args) throws IOException, ConfigException {
        config = new Config();
        int i = 0;
        while (i < args.length) {
            if (args[i].equalsIgnoreCase(CONFIG_FLAG) || args[i].equalsIgnoreCase(CONFIG_LONG_FLAG)) {
                config.read(new File(args[++i]));
            }
            else {
                System.out.println("Unrecognised option: " + args[i]);
            }
            ++i;
        }
        codec = new LegacyMessageCodec();
	agentManager = new LegacyAgentManager();
    }

    /**
       Start the kernel!
       @param args The command line arguments.
    */
    public static void main(String[] args) {
        try {
            new Kernel(args).runSimulation();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ConfigException e) {
            e.printStackTrace();
        }
    }

    /**
       Start the kernel, run the simulation and clean up.
       @throws IOException If there is a problem connecting to the GIS.
       @throws InterruptedException If the kernel is interrupted before the simulation has finished.
    */
    public void runSimulation() throws IOException, InterruptedException {
        connectToGIS();
        openSockets();
        waitForSimulatorsAndAgents();
        waitForSimulationToFinish();
        cleanUp();
    }

    private void connectToGIS() throws IOException, InterruptedException {
	System.out.println("Connecting to GIS...");
        worldModel = new WorldModel();
        CountDownLatch latch = new CountDownLatch(1);
        int gisPort = config.getIntValue("gis_port");
        gisConnection = new TCPConnection(codec, gisPort);
        gisConnection.addConnectionListener(new GISConnectionListener(latch, worldModel, gisConnection));
        gisConnection.startup();
        gisConnection.sendMessage(new KGConnect(0));
        // Wait for a reply
        latch.await();
	// Tell the agent manager about the world
	agentManager.setWorldModel(worldModel);
    }

    private void openSockets() throws IOException {
	connectionManager = new ConnectionManager();
	ConnectionManagerListener listener = new ConnectionManagerListener() {
		public void newConnection(Connection c) {
		    System.out.println("New connection: " + c);
		    agentManager.newConnection(c);
		    c.startup();
		}
	    };
	connectionManager.listen(config.getIntValue("port"), codec, listener);
    }

    private void waitForSimulatorsAndAgents() {
    }

    private void waitForSimulationToFinish() {
    }

    private void cleanUp() {
        gisConnection.shutdown();
    }

    /**
       Listener for the GIS connection.
    */
    private static class GISConnectionListener implements ConnectionListener {
        private CountDownLatch latch;
        private WorldModel model;
        private Connection gisConnection;

        public GISConnectionListener(CountDownLatch latch, WorldModel model, Connection gisConnection) {
            this.latch = latch;
            this.model = model;
            this.gisConnection = gisConnection;
        }

        public void messageReceived(Message m) {
            if (m instanceof GKConnectOK) {
                try {
                    // Update the internal world model
		    model.removeAllEntities();
		    model.addEntities(((GKConnectOK)m).getWorldModel().getAllEntities());
                    // Send an acknowledgement
                    gisConnection.sendMessage(new KGAcknowledge());
		    System.out.println("GIS connected OK");
                    // Trigger the countdown latch
                    latch.countDown();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (m instanceof GKConnectError) {
                System.err.println("Error: " + ((GKConnectError)m).getReason());
            }
        }
    }
}