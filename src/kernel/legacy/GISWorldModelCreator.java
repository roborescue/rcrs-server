package kernel.legacy;

import kernel.WorldModelCreator;
import kernel.KernelException;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.TCPConnection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.WorldModel;

import rescuecore2.version0.entities.RescueObject;
import rescuecore2.version0.messages.Version0MessageFactory;
import rescuecore2.version0.messages.GKConnectOK;
import rescuecore2.version0.messages.GKConnectError;
import rescuecore2.version0.messages.KGConnect;
import rescuecore2.version0.messages.KGAcknowledge;

/**
   A WorldModelCreator that talks to the GIS.
 */
public class GISWorldModelCreator implements WorldModelCreator<RescueObject, IndexedWorldModel> {
    @Override
    public IndexedWorldModel buildWorldModel(Config config) throws KernelException {
        System.out.println("Connecting to GIS...");
        IndexedWorldModel world = new IndexedWorldModel(config.getIntValue("vision"));
        CountDownLatch latch = new CountDownLatch(1);
        int gisPort = config.getIntValue("gis_port");
        Connection conn;
        try {
            conn = new TCPConnection(Version0MessageFactory.INSTANCE, gisPort);
            conn.addConnectionListener(new GISConnectionListener(latch, world, conn));
            conn.startup();
            conn.sendMessage(new KGConnect());
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
        world.index();
        return world;
    }

    /**
       Listener for the GIS connection.
    */
    private static class GISConnectionListener implements ConnectionListener {
        private CountDownLatch latch;
        private IndexedWorldModel model;
        private Connection gisConnection;

        public GISConnectionListener(CountDownLatch latch, IndexedWorldModel model, Connection gisConnection) {
            this.latch = latch;
            this.model = model;
            this.gisConnection = gisConnection;
        }

        public void messageReceived(Message m) {
            if (m instanceof GKConnectOK) {
                try {
                    // Update the internal world model
                    model.removeAllEntities();
                    model.addEntities(((GKConnectOK)m).getEntities());
                    // Send an acknowledgement
                    gisConnection.sendMessage(new KGAcknowledge());
                    System.out.println("GIS connected OK");
                    // Trigger the countdown latch
                    latch.countDown();
                }
                catch (IOException e) {
                    e.printStackTrace();
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