package kernel;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
   Abstract base class for ViewerManager implementations.
   @param <S> The subclass of WorldModel that this AgentManager understands.
   @param <T> The subclass of Entity that this AgentManager understands.
*/
public abstract class AbstractViewerManager<T extends Entity, S extends WorldModel<T>> implements ViewerManager<T, S> {
    private Set<ViewerManagerListener> listeners;
    private Set<Viewer<T>> allViewers;

    private S worldModel;

    /**
       Construct an AbstractViewerManager.
     */
    protected AbstractViewerManager() {
        listeners = new HashSet<ViewerManagerListener>();
        allViewers = new HashSet<Viewer<T>>();
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

    @Override
    public Collection<Viewer<T>> getAllViewers() throws InterruptedException {
        synchronized (allViewers) {
            return Collections.unmodifiableCollection(allViewers);
        }
    }
    /**
       Register a new viewer.
       @param v The new viewer.
     */
    protected void addViewer(Viewer<T> v) {
        synchronized (allViewers) {
            allViewers.add(v);
        }
    }

    @Override
    public final void setWorldModel(S world) {
        worldModel = world;
    }

    /**
       Get the world model.
       @return The world model.
     */
    protected S getWorldModel() {
        return worldModel;
    }

    /**
       Fire a 'viewer connected' event to all listeners.
       @param viewer The Viewer that has connected.
     */
    protected void fireViewerConnected(Viewer<T> viewer) {
        ViewerInfo info = new ViewerInfo(viewer.toString());
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