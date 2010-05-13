package kernel;

import rescuecore2.config.Config;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.connection.Connection;
import rescuecore2.connection.StreamConnection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.registry.Registry;
import rescuecore2.misc.Pair;

/**
   A class that knows how to connect components to the kernel using inline streams.
 */
public class InlineComponentLauncher extends ComponentLauncher {
    private ComponentManager manager;
    private Registry registry;

    /**
       Construct a new InlineComponentLauncher.
       @param manager The component manager.
       @param registry The registry.
       @param config The system configuration.
    */
    public InlineComponentLauncher(ComponentManager manager, Registry registry, Config config) {
        super(config);
        this.manager = manager;
        this.registry = registry;
    }

    @Override
    protected Connection makeConnection() throws ConnectionException {
        Pair<Connection, Connection> connections = StreamConnection.createConnectionPair(registry);
        manager.newConnection(connections.first());
        return connections.second();
    }
}
