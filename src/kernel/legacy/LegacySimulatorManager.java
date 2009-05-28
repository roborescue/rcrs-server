package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import kernel.AbstractSimulatorManager;
import kernel.Simulator;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.messages.SKConnect;
import rescuecore2.version0.messages.SKAcknowledge;
import rescuecore2.version0.messages.SKUpdate;
import rescuecore2.version0.messages.KSConnectOK;
import rescuecore2.version0.messages.Update;
import rescuecore2.version0.messages.AgentCommand;
import rescuecore2.version0.messages.Commands;

/**
   SimulatorManager implementation for classic Robocup Rescue.
 */
public class LegacySimulatorManager extends AbstractSimulatorManager<RescueEntity, IndexedWorldModel> {
    private Set<LegacySimulator> toAcknowledge;
    private int nextID;

    private final Object lock = new Object();

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
    public Collection<Simulator<RescueEntity, IndexedWorldModel>> getAllSimulators() throws InterruptedException {
        synchronized (lock) {
            while (!toAcknowledge.isEmpty()) {
                lock.wait(1000);
                System.out.println("Waiting for " + toAcknowledge.size() + " simulators to acknowledge");
            }
        }
        return super.getAllSimulators();
    }

    private boolean acknowledge(int id, Connection c) {
        synchronized (lock) {
            for (LegacySimulator next : toAcknowledge) {
                if (next.getID() == id && next.getConnection() == c) {
                    toAcknowledge.remove(next);
                    addSimulator(next);
                    lock.notifyAll();
                    fireSimulatorConnected(next);
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
        @Override
        public void messageReceived(Connection connection, Message msg) {
            if (msg instanceof SKConnect) {
                int id = getNextID();
                System.out.println("Simulator " + id + " connected");
                LegacySimulator sim = new LegacySimulator(connection, id);
                synchronized (lock) {
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