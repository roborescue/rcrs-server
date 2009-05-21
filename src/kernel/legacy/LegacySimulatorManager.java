package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;

import kernel.AbstractSimulatorManager;
import kernel.SimulatorInfo;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.version0.entities.RescueObject;
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
public class LegacySimulatorManager extends AbstractSimulatorManager<RescueObject, IndexedWorldModel> {
    private IndexedWorldModel worldModel;

    private Set<SimulatorData> toAcknowledge;
    private int nextID;

    private Set<SimulatorData> allSims;
    /** Map from simulator ID to update list. */
    private Map<Integer, Collection<RescueObject>> updates;

    private final Object lock = new Object();

    /**
       Create a LegacySimulatorManager.
    */
    public LegacySimulatorManager() {
        allSims = new HashSet<SimulatorData>();
        toAcknowledge = new HashSet<SimulatorData>();
        updates = new HashMap<Integer, Collection<RescueObject>>();
        nextID = 1;
    }

    @Override
    public void setWorldModel(IndexedWorldModel world) {
        worldModel = world;
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
                System.out.println("Waiting for " + toAcknowledge.size() + " simulators to acknowledge");
            }
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void sendToAll(Collection<? extends Message> messages) {
        Collection<SimulatorData> data = new HashSet<SimulatorData>();
        synchronized (lock) {
            data.addAll(allSims);
        }
        for (SimulatorData next : data) {
            try {
                if (!next.dead) {
                    next.connection.sendMessages(messages);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                next.dead = true;
            }
            catch (ConnectionException e) {
                e.printStackTrace();
                next.dead = true;
            }
        }
    }

    @Override
    public Collection<RescueObject> getAllUpdates() throws InterruptedException {
        Collection<RescueObject> result = new HashSet<RescueObject>();
        synchronized (lock) {
            // Wait until all simulators have sent an update
            while (updates.size() < allSims.size()) {
                lock.wait(1000);
                System.out.println("Waiting for " + (allSims.size() - updates.size()) + " simulator updates");
            }
            // Pull the results together
            for (Collection<RescueObject> next : updates.values()) {
                result.addAll(next);
            }
            updates.clear();
        }
        return result;
    }

    @Override
    public void sendUpdate(int time, Collection<RescueObject> updatedObjects) {
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
            for (SimulatorData next : toAcknowledge) {
                if (next.id == id && next.connection == c) {
                    toAcknowledge.remove(next);
                    allSims.add(next);
                    lock.notifyAll();
                    fireSimulatorConnected(new SimulatorInfo(next.connection.toString() + ": " + next.id));
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

    private void updateReceived(int id, Collection<RescueObject> entities) {
        synchronized (lock) {
            updates.put(id, entities);
            lock.notifyAll();
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
                SimulatorData data = new SimulatorData(id, connection);
                synchronized (lock) {
                    toAcknowledge.add(data);
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
            if (msg instanceof SKUpdate) {
                System.out.println("Received simulator update: " + msg);
                SKUpdate update = (SKUpdate)msg;
                updateReceived(update.getSimulatorID(), update.getUpdatedEntities());
            }
        }
    }

    private static class SimulatorData {
        int id;
        Connection connection;
        boolean dead;

        SimulatorData(int id, Connection c) {
            this.id = id;
            this.connection = c;
            this.dead = false;
        }
    }
}