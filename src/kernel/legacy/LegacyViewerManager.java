package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

import kernel.AbstractViewerManager;
import kernel.Viewer;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;

import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.messages.VKConnect;
import rescuecore2.version0.messages.VKAcknowledge;
import rescuecore2.version0.messages.KVConnectOK;

/**
   ViewerManager implementation for classic Robocup Rescue.
 */
public class LegacyViewerManager extends AbstractViewerManager<RescueEntity, IndexedWorldModel> {
    private Set<LegacyViewer> toAcknowledge;

    /**
       Create a LegacyViewerManager.
    */
    public LegacyViewerManager() {
        toAcknowledge = new HashSet<LegacyViewer>();
    }

    @Override
    public void newConnection(Connection c) {
        c.addConnectionListener(new ViewerConnectionListener());
    }

    @Override
    public Collection<Viewer<RescueEntity>> getAllViewers() throws InterruptedException {
        synchronized (toAcknowledge) {
            while (!toAcknowledge.isEmpty()) {
                toAcknowledge.wait(1000);
                System.out.println("Waiting for " + toAcknowledge.size() + " viewers to acknowledge");
            }
        }
        return super.getAllViewers();
    }

    @Override
    public void shutdown() {
    }

    private boolean acknowledge(Connection c) {
        synchronized (toAcknowledge) {
            for (LegacyViewer next : toAcknowledge) {
                if (next.getConnection() == c) {
                    toAcknowledge.remove(next);
                    addViewer(next);
                    toAcknowledge.notifyAll();
                    fireViewerConnected(next);
                    return true;
                }
            }
            return false;
        }
    }

    private class ViewerConnectionListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection connection, Message msg) {
            if (msg instanceof VKConnect) {
                System.out.println("Viewer connected");
                LegacyViewer viewer = new LegacyViewer(connection);
                synchronized (toAcknowledge) {
                    toAcknowledge.add(viewer);
                }
                // Send an OK
                viewer.send(Collections.singleton(new KVConnectOK(getWorldModel().getAllEntities())));
            }
            if (msg instanceof VKAcknowledge) {
                if (acknowledge(connection)) {
                    System.out.println("Viewer acknowledged");
                }
                else {
                    System.out.println("Unexpected viewer acknowledge");
                }
            }
        }
    }
}