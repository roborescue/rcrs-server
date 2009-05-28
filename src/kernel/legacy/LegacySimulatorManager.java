package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

import kernel.AbstractSimulatorManager;
import kernel.Simulator;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.messages.SKConnect;
import rescuecore2.version0.messages.SKAcknowledge;
import rescuecore2.version0.messages.KSConnectOK;
/**
   SimulatorManager implementation for classic Robocup Rescue.
 */
public class LegacySimulatorManager extends AbstractSimulatorManager<RescueEntity, IndexedWorldModel> {
    private Set<LegacySimulator> toAcknowledge;
    private int nextID;

    /**
       Create a LegacySimulatorManager.
    */
    public LegacySimulatorManager() {
        toAcknowledge = new HashSet<LegacySimulator>();
        nextID = 1;
    }

    @Override
    public void newConnection(Connection c) {
        c.addConnectionListener(new SimulatorConnectionListener());
    }

    @Override
    public Collection<Simulator<RescueEntity>> getAllSimulators() throws InterruptedException {
        synchronized (toAcknowledge) {
            while (!toAcknowledge.isEmpty()) {
                toAcknowledge.wait(1000);
                System.out.println("Waiting for " + toAcknowledge.size() + " simulators to acknowledge");
            }
        }
        return super.getAllSimulators();
    }

    private boolean acknowledge(int id, Connection c) {
        synchronized (toAcknowledge) {
            for (LegacySimulator next : toAcknowledge) {
                if (next.getID() == id && next.getConnection() == c) {
                    toAcknowledge.remove(next);
                    addSimulator(next);
                    toAcknowledge.notifyAll();
                    fireSimulatorConnected(next);
                    return true;
                }
            }
            return false;
        }
    }

    private int getNextID() {
        synchronized (toAcknowledge) {
            return nextID++;
        }
    }

    private class SimulatorConnectionListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection connection, Message msg) {
            if (msg instanceof SKConnect) {
                int id = getNextID();
                System.out.println("Simulator " + id + " connected");
                LegacySimulator sim = new LegacySimulator(connection, id);
                synchronized (toAcknowledge) {
                    toAcknowledge.add(sim);
                }
                // Send an OK
                sim.send(Collections.singleton(new KSConnectOK(id, getWorldModel().getAllEntities())));
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
}