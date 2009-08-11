package kernel.standard;

import kernel.KernelException;
import kernel.WorldModelCreator;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.TCPConnection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.GKConnectOK;
import rescuecore2.messages.control.GKConnectError;
import rescuecore2.messages.control.KGConnect;
import rescuecore2.messages.control.KGAcknowledge;

import rescuecore2.standard.entities.StandardWorldModel;

/**
   A WorldModelCreator that talks to the GIS to build StandardEntities.
 */
public class StandardWorldModelCreator implements WorldModelCreator {
    private static final String PORT_KEY = "gis.port";

    /**
       Create a new StandardWorldModel.
       @param config The config to use.
       @return A new world model.
       @throws KernelException If there is a problem building the world model.
    */
    public WorldModel<? extends Entity> buildWorldModel(Config config) throws KernelException {
        System.out.println("Connecting to GIS...");
        StandardWorldModel world = new StandardWorldModel();
        CountDownLatch latch = new CountDownLatch(1);
        int gisPort = config.getIntValue(PORT_KEY);
        Connection conn;
        try {
            conn = new TCPConnection(gisPort);
            conn.addConnectionListener(new GISConnectionListener(latch, world));
            conn.startup();
            conn.sendMessage(new KGConnect(1));
        }
        catch (IOException e) {
            throw new KernelException("Couldn't connect to GIS", e);
        }
        catch (ConnectionException e) {
            throw new KernelException("Couldn't connect to GIS", e);
        }
        // Wait for a reply
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            throw new KernelException("Interrupted while connecting to GIS", e);
        }
        conn.shutdown();
        world.index(config.getIntValue("vision"));
        return world;
    }

    /**
       Listener for the GIS connection.
    */
    private static class GISConnectionListener implements ConnectionListener {
        private CountDownLatch latch;
        private StandardWorldModel model;

        public GISConnectionListener(CountDownLatch latch, StandardWorldModel model) {
            this.latch = latch;
            this.model = model;
        }

        public void messageReceived(Connection c, Message m) {
            if (m instanceof GKConnectOK) {
                try {
                    // Update the internal world model
                    model.removeAllEntities();
                    model.addEntities(((GKConnectOK)m).getEntities());
                    // Send an acknowledgement
                    c.sendMessage(new KGAcknowledge());
                    System.out.println("GIS connected OK");
                    // Trigger the countdown latch
                    latch.countDown();
                }
                catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
            if (m instanceof GKConnectError) {
                System.err.println("Error: " + ((GKConnectError)m).getReason());
            }
        }
    }
}