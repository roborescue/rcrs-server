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
   @param <T> The subclass of Entity that this simulator understands.
 */
public abstract class AbstractSimulator<T extends Entity> implements Simulator<T> {
    private Connection connection;
    private Map<Integer, Collection<T>> updates;

    /**
       Construct a new abstract simulator.
       @param c The connection this simulator is using.
     */
    protected AbstractSimulator(Connection c) {
        this.connection = c;
        updates = new HashMap<Integer, Collection<T>>();
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
    public Collection<T> getUpdates(int timestep) throws InterruptedException {
        Collection<T> result = null;
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

    @Override
    public String toString() {
        return connection.toString();
    }

    /**
       Register an update from the simulator.
       @param time The timestep of the update.
       @param u The set of updated entities.
     */
    protected void updateReceived(int time, Collection<? extends T> u) {
        synchronized (updates) {
            Collection<T> c = updates.get(time);
            if (c == null) {
                c = new HashSet<T>();
                updates.put(time, c);
            }
            c.addAll(u);
        }
    }
}