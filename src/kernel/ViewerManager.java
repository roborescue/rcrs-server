package kernel;

import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

import java.util.Collection;

/**
   This class manages connections from viewers.
   @param <S> The subclass of WorldModel that this manager understands.
   @param <T> The subclass of Entity that this manager understands.
 */
public interface ViewerManager<T extends Entity, S extends WorldModel<? super T>> extends ConnectionManagerListener, WorldModelAware<S> {
    /**
       Add a ViewerManagerListener.
       @param l The listener to add.
    */
    void addViewerManagerListener(ViewerManagerListener l);

    /**
       Remove a ViewerManagerListener.
       @param l The listener to add.
    */
    void removeViewerManagerListener(ViewerManagerListener l);

    /**
       Wait until all viewers have connected.
       @throws InterruptedException If this thread is interrupted while waiting for viewers.
    */
    void waitForViewers() throws InterruptedException;

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
       Send a set of agent commands to all viewers.
       @param time The current time.
       @param commands The agent commands to send.
     */
    void sendAgentCommands(int time, Collection<? extends Message> commands);

    /**
       Shut this manager down.
     */
    void shutdown();
}