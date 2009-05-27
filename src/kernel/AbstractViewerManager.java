package kernel;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.io.IOException;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.messages.Message;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;

/**
   Abstract base class for ViewerManager implementations.
   @param <S> The subclass of WorldModel that this AgentManager understands.
   @param <T> The subclass of Entity that this AgentManager understands.
*/
public abstract class AbstractViewerManager<T extends Entity, S extends WorldModel<T>> implements ViewerManager<T, S> {
    private Set<ViewerManagerListener> listeners;
    private Set<Viewer> allViewers;

    private S worldModel;

    /**
       Construct an AbstractViewerManager.
     */
    protected AbstractViewerManager() {
        listeners = new HashSet<ViewerManagerListener>();
        allViewers = new HashSet<Viewer>();
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
       Register a new viewer.
       @param v The new viewer.
     */
    protected void addViewer(Viewer v) {
        synchronized (allViewers) {
            allViewers.add(v);
        }
    }

    @Override
    public void sendToAll(Collection<? extends Message> messages) {
        Collection<Viewer> data = new HashSet<Viewer>();
        synchronized (allViewers) {
            data.addAll(allViewers);
        }
        for (Viewer next : data) {
            next.send(messages);
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
    protected void fireViewerConnected(Viewer viewer) {
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

    /**
       Internal representation of a viewer.
     */
    protected static class Viewer {
        private Connection connection;

        /**
           Construct a viewer.
           @param c The connection for talking to the viewer.
         */
        public Viewer(Connection c) {
            this.connection = c;
        }

        /**
           Send some messages to the viewer.
           @param m The messages to send.
         */
        public void send(Collection<? extends Message> m) {
            if (!connection.isAlive()) {
                return;
            }
            try {
                connection.sendMessages(m);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ConnectionException e) {
                e.printStackTrace();
            }
        }

        /**
           Get the connection for talking to this viewer.
           @return The connection.
         */
        public Connection getConnection() {
            return connection;
        }

        @Override
        public String toString() {
            return connection.toString();
        }
    }
}