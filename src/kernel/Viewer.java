package kernel;

import rescuecore2.connection.Connection;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.worldmodel.Entity;

import java.util.Collection;

/**
   This class is the kernel interface to a viewer.
 */
public interface Viewer {
    /**
       Send a set of messages to this viewer.
       @param m The messages to send.
     */
    void send(Collection<? extends Message> m);

    /**
       Send an update message to this viewer.
       @param time The simulation time.
       @param updates The updated entities.
    */
    void sendUpdate(int time, Collection<? extends Entity> updates);

    /**
       Send a set of agent commands to this viewer.
       @param time The current time.
       @param commands The agent commands to send.
     */
    void sendAgentCommands(int time, Collection<? extends Command> commands);

    /**
       Shut this viewer down.
     */
    void shutdown();

    /**
       Get this viewer's connection.
       @return The connection to the viewer.
     */
    Connection getConnection();
}