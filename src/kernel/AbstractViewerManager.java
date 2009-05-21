package kernel;

import java.util.Set;
import java.util.HashSet;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
   Abstract base class for ViewerManager implementations.
   @param <S> The subclass of WorldModel that this AgentManager understands.
   @param <T> The subclass of Entity that this AgentManager understands.
*/
public abstract class AbstractViewerManager<T extends Entity, S extends WorldModel<T>> implements ViewerManager<T, S> {
    private Set<ViewerManagerListener> listeners;

    /**
       Construct an AbstractViewerManager.
     */
    protected AbstractViewerManager() {
        listeners = new HashSet<ViewerManagerListener>();
    }

    @Override
    public void addViewerManagerListener(ViewerManagerListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeViewerManagerListener(ViewerManagerListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
       Fire a 'viewer connected' event to all listeners.
       @param info The ViewerInfo to send.
     */
    protected void fireViewerConnected(ViewerInfo info) {
        for (ViewerManagerListener next : getListeners()) {
            next.viewerConnected(info);
        }
    }

    private Set<ViewerManagerListener> getListeners() {
        Set<ViewerManagerListener> result;
        synchronized (listeners) {
            result = new HashSet<ViewerManagerListener>(listeners);
        }
        return result;
    }
}