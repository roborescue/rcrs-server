package kernel;

import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

import java.util.Collection;

/**
   This class manages connections from simulators.
   @param <S> The subclass of WorldModel that this manager understands.
   @param <T> The subclass of Entity that this manager understands.
 */
public interface SimulatorManager<T extends Entity, S extends WorldModel<? super T>> extends ConnectionManagerListener, WorldModelAware<S> {
    /**
       Add a SimulatorManagerListener.
       @param l The listener to add.
    */
    void addSimulatorManagerListener(SimulatorManagerListener l);

    /**
       Remove a SimulatorManagerListener.
       @param l The listener to add.
    */
    void removeSimulatorManagerListener(SimulatorManagerListener l);

    /**
       Wait until all simulators have connected.
       @throws InterruptedException If this thread is interrupted while waiting for simulators.
    */
    void waitForSimulators() throws InterruptedException;

    /**
       Send a set of messages to all simulators.
       @param m The messages to send.
     */
    void sendToAll(Collection<? extends Message> m);

    /**
       Get updates from all simulators. This method should block until all updates are available.
       @return A collection of entities representing the updates from simulators.
       @throws InterruptedException If this thread is interrupted while waiting for updates.
    */
    Collection<T> getAllUpdates() throws InterruptedException;

    /**
       Send an update message to all simulators.
       @param time The simulation time.
       @param updates The updated entities.
    */
    void sendUpdate(int time, Collection<T> updates);

    /**
       Send a set of agent commands to all simulators.
       @param time The current time.
       @param commands The agent commands to send.
     */
    void sendAgentCommands(int time, Collection<? extends Message> commands);

    /**
       Shut this manager down.
     */
    void shutdown();
}