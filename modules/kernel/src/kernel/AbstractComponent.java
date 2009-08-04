package kernel;

import rescuecore2.messages.Message;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;

import java.util.Collection;

/**
   Abstract base class for KernelComponent implementations.
 */
public abstract class AbstractComponent implements KernelComponent {
    private Connection connection;

    /**
       Construct a new abstract component.
       @param c The connection this component is using.
     */
    protected AbstractComponent(Connection c) {
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
}