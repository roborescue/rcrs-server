package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;

/**
   A class that knows how to connect components to the kernel.
 */
public class ComponentLauncher implements RequestIDGenerator {
    private Connection connection;
    private int nextRequestID;

    /**
       Construct a new ComponentLauncher that will use a particular connection to connect components. The same connection will be re-used for all components.
       @param connection The Connection to use.
    */
    public ComponentLauncher(Connection connection) {
        this.connection = connection;
        nextRequestID = 1;
    }

    /**
       Connect a Component to the kernel. Throws a ComponentConnectionException if the connection fails due to a kernel ConnectError message.
       @param c The component to connect.
       @throws InterruptedException If the thread is interrupted before the connection attempt completes.
       @throws ConnectionException If there is a problem communicating with the kernel.
       @throws ComponentConnectionException If the connection fails.
    */
    public void connect(Component c) throws InterruptedException, ConnectionException, ComponentConnectionException {
        c.connect(connection, this);
    }

    @Override
    public int generateRequestID() {
        synchronized (this) {
            return nextRequestID++;
        }
    }
}