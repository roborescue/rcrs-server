package kernel;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;
import rescuecore2.messages.Message;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.connection.ConnectionException;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

/**
   Abstract base class for Simulator implementations.
 */
public abstract class AbstractSimulator<T extends Entity, S extends WorldModel<? super T>> implements Simulator<T, S> {
    private Connection connection;
    private Map<Integer, Collection<T>> updates;
    private S worldModel;

    /**
       Construct a new abstract simulator.
       @param c The connection this simulator is using.
     */
    protected AbstractSimulator(Connection c) {
        this.connection = c;
        updates = new HashMap<Integer, Collection<T>>();
    }

    @Override
    public void setWorldModel(S model) {
        worldModel = model;
    }

    protected S getWorldModel() {
        return worldModel;
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