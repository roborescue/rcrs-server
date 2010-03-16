package kernel;

import rescuecore2.messages.Message;
import rescuecore2.messages.control.Shutdown;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.log.Logger;

import java.util.Collection;
import java.util.Collections;

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
            Logger.error("Error sending message", e);
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void shutdown() {
        send(new Shutdown());
        connection.shutdown();
    }

    @Override
    public String getName() {
        return name;
    }

    /**
       Send a single message.
       @param message The message to send.
    */
    protected void send(Message message) {
        send(Collections.singleton(message));
    }
}
