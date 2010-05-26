package rescuecore2.components;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.registry.Registry;

/**
   Top-level interface for components of the Robocup Rescue simulation. Agents, simulators and viewers are all components.
 */
public interface Component {
    /**
       Initialise this component before connection.
       @throws ComponentInitialisationException If there is a problem initialising the component.
     */
    void initialise() throws ComponentInitialisationException;

    /**
       Shut this component down.
     */
    void shutdown();

    /**
       Get the name of this component. This is useful for debugging. Often a class name will be sufficient.
       @return A name.
    */
    String getName();

    /**
       Get the registry this component would like to use for its connection.
       @param parent The parent registry.
       @return The preferred registry.
    */
    Registry getPreferredRegistry(Registry parent);

    /**
       Get the preferred log context for this component.
       @return The preferred log context for this component.
    */
    String getPreferredLogContext();

    /**
       Connect this component to the kernel.
       @param connection The Connection to use.
       @param generator The RequestIDGenerator to use.
       @param config The system configuration.
       @throws ConnectionException If there is a problem communicating with the kernel.
       @throws ComponentConnectionException If the connection fails.
       @throws InterruptedException If the thread is interrupted before the connection attempt completes.
    */
    void connect(Connection connection, RequestIDGenerator generator, Config config) throws ConnectionException, ComponentConnectionException, InterruptedException;
}
