package rescuecore2.components;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;

/**
   Top-level interface for components of the Robocup Rescue simulation. Agents, simulators and viewers are all components.
 */
public interface Component {
    /**
       Initialise this component before connection. Subclasses should NOT perform config-specific initialisation at this point because the kernel may send new configuration information when the connection is made.
       @param config The system configuration.
       @throws ComponentInitialisationException If there is a problem initialising the component.
     */
    void initialise(Config config) throws ComponentInitialisationException;

    /**
       Get the name of this component. This is useful for debugging. Often a class name will be sufficient.
       @return A name.
    */
    String getName();

    /**
       Connect this component to the kernel.
       @param connection The Connection to use.
       @param generator The RequestIDGenerator to use.
       @throws ConnectionException If there is a problem communicating with the kernel.
       @throws ComponentConnectionException If the connection fails.
       @throws InterruptedException If the thread is interrupted before the connection attempt completes.
    */
    void connect(Connection connection, RequestIDGenerator generator) throws ConnectionException, ComponentConnectionException, InterruptedException;
}