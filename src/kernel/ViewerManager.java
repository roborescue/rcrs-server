package kernel;

import rescuecore2.connection.ConnectionManagerListener;
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
       Get all Viewers. This method may block if it needs to wait for viewers to connect.
       @return The set of all viewers.
       @throws InterruptedException If this thread is interrupted while waiting for viewers.
    */
    Collection<Viewer<T>> getAllViewers() throws InterruptedException;

    /**
       Shut this manager down.
     */
    void shutdown();
}