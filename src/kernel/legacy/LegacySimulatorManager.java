package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.io.IOException;

import kernel.SimulatorManager;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.version0.messages.SKConnect;
import rescuecore2.version0.messages.SKAcknowledge;
import rescuecore2.version0.messages.KSConnectOK;

/**
   SimulatorManager implementation for classic Robocup Rescue.
 */
public class LegacySimulatorManager implements SimulatorManager {
    private WorldModel worldModel;

    private Set<SimulatorInfo> toAcknowledge;
    private int nextID;

    private final Object lock = new Object();

    /**
       Start a LegacySimulatorManager based on a world model.
       @param m The world model that contains all entities.
    */
    public LegacySimulatorManager(WorldModel m) {
        worldModel = m;
        toAcknowledge = new HashSet<SimulatorInfo>();
        nextID = 1;
    }

    @Override
    public void newConnection(Connection c) {
        c.addConnectionListener(new SimulatorConnectionListener(c));
    }

    @Override
    public void waitForAcknowledgements() throws InterruptedException {
        synchronized (lock) {
            while (!toAcknowledge.isEmpty()) {
                lock.wait(1000);
            }
        }
    }

    @Override
    public void shutdown() {
    }

    private boolean acknowledge(int id, Connection c) {
        synchronized (lock) {
            for (SimulatorInfo next : toAcknowledge) {
                if (next.id == id && next.connection == c) {
                    toAcknowledge.remove(next);
                    lock.notifyAll();
                    return true;
                }
            }
            return false;
        }
    }

    private int getNextID() {
        synchronized (lock) {
            return nextID++;
        }
    }

    private class SimulatorConnectionListener implements ConnectionListener {
        private Connection connection;

        public SimulatorConnectionListener(Connection c) {
            connection = c;
        }

        @Override
        public void messageReceived(Message msg) {
            if (msg instanceof SKConnect) {
                int id = getNextID();
                System.out.println("Simulator " + id + " connected");
                SimulatorInfo info = new SimulatorInfo(id, connection);
                synchronized (lock) {
                    toAcknowledge.add(info);
                }
                // Send an OK
                try {
                    connection.sendMessage(new KSConnectOK(id, worldModel.getAllEntities()));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
            if (msg instanceof SKAcknowledge) {
                int id = ((SKAcknowledge)msg).getSimulatorID();
                if (acknowledge(id, connection)) {
                    System.out.println("Simulator " + id + " acknowledged");
                }
                else {
                    System.out.println("Unexpected acknowledge from simulator " + id);
                }
            }
        }
    }

    private static class SimulatorInfo {
        int id;
        Connection connection;

        SimulatorInfo(int id, Connection c) {
            this.id = id;
            this.connection = c;
        }
    }
}