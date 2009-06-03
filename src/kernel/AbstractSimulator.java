package kernel;

import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.Message;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

/**
   Abstract base class for Simulator implementations.
 */
public abstract class AbstractSimulator implements Simulator {
    private Connection connection;
    private Map<Integer, Collection<Entity>> updates;

    /**
       Construct a new abstract simulator.
       @param c The connection this simulator is using.
     */
    protected AbstractSimulator(Connection c) {
        this.connection = c;
        updates = new HashMap<Integer, Collection<Entity>>();
    }

    @Override
    public void send(Collection<? extends Message> messages) {
        if (!connection.isAlive()) {
            return;
        }
        try {
            connection.sendMessages(messages);
        }
        catch (ConnectionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<Entity> getUpdates(int timestep) throws InterruptedException {
        Collection<Entity> result = null;
        synchronized (updates) {
            while (result == null) {
                result = updates.get(timestep);
                if (result == null) {
                    updates.wait(1000);
                }
            }
        }
        return result;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void shutdown() {
        connection.shutdown();
    }

    /**
       Register an update from the simulator.
       @param time The timestep of the update.
       @param u The set of updated entities.
     */
    protected void updateReceived(int time, Collection<? extends Entity> u) {
        synchronized (updates) {
            Collection<Entity> c = updates.get(time);
            if (c == null) {
                c = new HashSet<Entity>();
                updates.put(time, c);
            }
            c.addAll(u);
            updates.notifyAll();
        }
    }
}