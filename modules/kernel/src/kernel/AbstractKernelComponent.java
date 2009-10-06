package kernel;

import rescuecore2.messages.Message;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;

import java.util.Collection;

/**
   Abstract base class for KernelComponent implementations.
 */
public abstract class AbstractKernelComponent implements KernelComponent {
    private Connection connection;
    private String name;

    /**
       Construct a new abstract component.
       @param name The name of this component.
       @param c The connection this component is using.
     */
    protected AbstractKernelComponent(String name, Connection c) {
        this.name = name;
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
    public String getName() {
        return name;
    }
}