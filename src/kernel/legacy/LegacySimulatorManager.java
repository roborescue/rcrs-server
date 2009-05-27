package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import kernel.AbstractSimulatorManager;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
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
    private Set<Simulator> toAcknowledge;
    private int nextID;

    /** Map from simulator ID to update list. */
    private Map<Integer, Collection<RescueEntity>> updates;

    private final Object lock = new Object();

    /**
       Create a LegacySimulatorManager.
    */
    public LegacySimulatorManager() {
        toAcknowledge = new HashSet<Simulator>();
        updates = new HashMap<Integer, Collection<RescueEntity>>();
        nextID = 1;
    }

    @Override
    public void newConnection(Connection c) {
        c.addConnectionListener(new SimulatorConnectionListener());
    }

    @Override
    public void waitForSimulators() throws InterruptedException {
        synchronized (lock) {
            while (!toAcknowledge.isEmpty()) {
                lock.wait(1000);
                System.out.println("Waiting for " + toAcknowledge.size() + " simulators to acknowledge");
            }
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Collection<RescueEntity> getAllUpdates() throws InterruptedException {
        Collection<RescueEntity> result = new HashSet<RescueEntity>();
        synchronized (lock) {
            // Wait until all simulators have sent an update
            while (updates.size() < getAllSimulators().size()) {
                lock.wait(1000);
                System.out.println("Waiting for " + (getAllSimulators().size() - updates.size()) + " simulator updates");
            }
            // Pull the results together
            for (Collection<RescueEntity> next : updates.values()) {
                result.addAll(next);
            }
            updates.clear();
        }
        return result;
    }

    @Override
    public void sendUpdate(int time, Collection<RescueEntity> updatedObjects) {
        sendToAll(Collections.singleton(new Update(time, updatedObjects)));
    }

    @Override
    public void sendAgentCommands(int time, Collection<? extends Message> commands) {
        Collection<AgentCommand> agentCommands = new ArrayList<AgentCommand>();
        for (Message next : commands) {
            if (next instanceof AgentCommand) {
                agentCommands.add((AgentCommand)next);
            }
        }
        sendToAll(Collections.singleton(new Commands(time, agentCommands)));
    }

    private boolean acknowledge(int id, Connection c) {
        synchronized (lock) {
            for (Simulator next : toAcknowledge) {
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

    private void updateReceived(int id, Collection<RescueEntity> entities) {
        synchronized (lock) {
            updates.put(id, entities);
            lock.notifyAll();
        }
    }

    private class SimulatorConnectionListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection connection, Message msg) {
            if (msg instanceof SKConnect) {
                int id = getNextID();
                System.out.println("Simulator " + id + " connected");
                Simulator sim = new Simulator(id, connection);
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
            if (msg instanceof SKUpdate) {
                System.out.println("Received simulator update: " + msg);
                SKUpdate update = (SKUpdate)msg;
                updateReceived(update.getSimulatorID(), update.getUpdatedEntities());
            }
        }
    }
}