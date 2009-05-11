package kernel;

import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

import java.util.Collection;

/**
   This class manages connections from viewers.
   @param <T> The subclass of Entity that this manager understands.
 */
public interface ViewerManager<T extends Entity> extends ConnectionManagerListener {
    /**
       Set the world model.
       @param world The new world model.
    */
    void setWorldModel(WorldModel<T> world);

    /**
       Wait until all viewers have acknowledged.
       @throws InterruptedException If this thread is interrupted while waiting for viewers.
    */
    void waitForAcknowledgements() throws InterruptedException;

    /**
       Send a set of messages to all viewers.
       @param m The messages to send.
     */
    void sendToAll(Collection<? extends Message> m);

    /**
       Send an update message to all viewers.
       @param time The simulation time.
       @param updates The updated entities.
    */
    void sendUpdate(int time, Collection<T> updates);

    /**
       Shut this manager down.
     */
    void shutdown();
}