package kernel;

import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

import java.util.Collection;

/**
   This class manages connections from simulators.
   @param <T> The subclass of Entity that this manager understands.
 */
public interface SimulatorManager<T extends Entity> extends ConnectionManagerListener {
    /**
       Set the world model.
       @param world The new world model.
    */
    void setWorldModel(WorldModel<T> world);

    /**
       Wait until all simulators have acknowledged.
       @throws InterruptedException If this thread is interrupted while waiting for simulators.
    */
    void waitForAcknowledgements() throws InterruptedException;

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
       Shut this manager down.
     */
    void shutdown();
}