package rescuecore2.components;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.registry.Registry;
import rescuecore2.log.Logger;

/**
   A class that knows how to connect components to the kernel.
 */
public abstract class ComponentLauncher implements RequestIDGenerator {
    private Config config;
    private int nextRequestID;
    private Registry defaultRegistry;

    /**
       Construct a new ComponentLauncher.
       @param config The system configuration.
    */
    public ComponentLauncher(Config config) {
        this.config = config;
        nextRequestID = 1;
        defaultRegistry = Registry.SYSTEM_REGISTRY;
    }

    /**
       Connect a Component to the kernel. Throws a ComponentConnectionException if the connection fails due to a kernel ConnectError message.
       @param c The component to connect.
       @throws InterruptedException If the thread is interrupted before the connection attempt completes.
       @throws ConnectionException If there is a problem communicating with the kernel.
       @throws ComponentConnectionException If the connection fails.
    */
    public void connect(Component c) throws InterruptedException, ConnectionException, ComponentConnectionException {
        Connection connection = makeConnection();
        connection.setName("Connection from " + c.getName());
        Logger.pushLogContext(c.getPreferredLogContext());
        connection.setRegistry(c.getPreferredRegistry(defaultRegistry));
        connection.startup();
        try {
            c.connect(connection, this, new Config(config));
        }
        finally {
            Logger.popLogContext();
        }
    }

    @Override
    public int generateRequestID() {
        synchronized (this) {
            return nextRequestID++;
        }
    }

    /**
       Set the default registry for new connections.
       @param registry The new default registry.
    */
    public void setDefaultRegistry(Registry registry) {
        defaultRegistry = registry;
    }

    /**
       Get the default registry for new connections.
       @return The default registry.
    */
    public Registry getDefaultRegistry() {
        return defaultRegistry;
    }

    /**
       Make a new connection.
       @return The new connection.
       @throws ConnectionException If there is a problem creating the connection.
    */
    protected abstract Connection makeConnection() throws ConnectionException;
}
