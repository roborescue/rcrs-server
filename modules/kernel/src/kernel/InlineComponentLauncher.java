package kernel;

import rescuecore2.config.Config;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.connection.Connection;
import rescuecore2.connection.StreamConnection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.misc.Pair;

/**
   A class that knows how to connect components to the kernel using inline streams.
 */
public class InlineComponentLauncher extends ComponentLauncher {
    private ComponentManager manager;

    /**
       Construct a new InlineComponentLauncher.
       @param manager The component manager.
       @param config The system configuration.
    */
    public InlineComponentLauncher(ComponentManager manager, Config config) {
        super(config);
        this.manager = manager;
    }

    @Override
    protected Connection makeConnection() throws ConnectionException {
        Pair<Connection, Connection> connections = StreamConnection.createConnectionPair();
        connections.first().setRegistry(getDefaultRegistry());
        connections.first().startup();
        manager.newConnection(connections.first());
        return connections.second();
    }
}
