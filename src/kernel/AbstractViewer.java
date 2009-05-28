package kernel;

import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.Message;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;

import java.util.Collection;

/**
   Abstract base class for Viewer implementations.
   @param <T> The subclass of Entity that this viewer understands.
 */
public abstract class AbstractViewer<T extends Entity> implements Viewer<T> {
    private Connection connection;

    /**
       Construct a new abstract viewer.
       @param c The connection this viewer is using.
     */
    protected AbstractViewer(Connection c) {
        this.connection = c;
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
}