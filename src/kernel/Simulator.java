package kernel;

import rescuecore2.connection.Connection;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.worldmodel.Entity;

import java.util.Collection;

/**
   This class is the kernel interface to a simulator.
 */
public interface Simulator {
    /**
       Send a set of messages to this simulator.
       @param m The messages to send.
     */
    void send(Collection<? extends Message> m);

    /**
       Get updates from this simulator. This method may block until updates are available.
       @param time The timestep to get updates for.
       @return A collection of entities representing the updates from this simulator.
       @throws InterruptedException If this thread is interrupted while waiting for updates.
    */
    Collection<Entity> getUpdates(int time) throws InterruptedException;

    /**
       Send an update message to this simulator.
       @param time The simulation time.
       @param updates The updated entities.
    */
    void sendUpdate(int time, Collection<? extends Entity> updates);

    /**
       Send a set of agent commands to this simulator.
       @param time The current time.
       @param commands The agent commands to send.
     */
    void sendAgentCommands(int time, Collection<? extends Command> commands);

    /**
       Shut this simulator down.
     */
    void shutdown();

    /**
       Get this simulators's connection.
       @return The connection to the simulator.
     */
    Connection getConnection();
}