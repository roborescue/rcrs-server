package kernel;

import rescuecore2.connection.Connection;
import rescuecore2.messages.Message;

import java.util.Collection;

/**
   This class is the kernel interface to components (agents, viewers, simulators).
 */
public interface KernelComponent {
    /**
       Send a set of messages to this component.
       @param m The messages to send.
     */
    void send(Collection<? extends Message> m);

    /**
       Shut this component down.
     */
    void shutdown();

    /**
       Get this component's connection.
       @return The connection to the component.
     */
    Connection getConnection();

    /**
       Get the name of this component.
       @return The name of the component.
     */
    String getName();
}
