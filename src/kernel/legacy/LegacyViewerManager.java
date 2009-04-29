package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.io.IOException;

import kernel.ViewerManager;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.version0.messages.VKConnect;
import rescuecore2.version0.messages.VKAcknowledge;
import rescuecore2.version0.messages.KVConnectOK;

/**
   ViewerManager implementation for classic Robocup Rescue.
 */
public class LegacyViewerManager implements ViewerManager {
    private WorldModel worldModel;

    private Set<ViewerInfo> toAcknowledge;

    private final Object lock = new Object();

    /**
       Start a LegacyViewerManager based on a world model.
       @param m The world model that contains all entities.
    */
    public LegacyViewerManager(WorldModel m) {
        worldModel = m;
        toAcknowledge = new HashSet<ViewerInfo>();
    }

    @Override
    public void newConnection(Connection c) {
        c.addConnectionListener(new ViewerConnectionListener(c));
    }

    @Override
    public void waitForAcknowledgements() throws InterruptedException {
        synchronized (lock) {
            while (!toAcknowledge.isEmpty()) {
                lock.wait(1000);
            }
        }
    }

    @Override
    public void shutdown() {
    }

    private boolean acknowledge() {
        synchronized (lock) {
            for (ViewerInfo next : toAcknowledge) {
                toAcknowledge.remove(next);
                lock.notifyAll();
                return true;
            }
            return false;
        }
    }

    private class ViewerConnectionListener implements ConnectionListener {
        private Connection connection;

        public ViewerConnectionListener(Connection c) {
            connection = c;
        }

        @Override
        public void messageReceived(Message msg) {
            if (msg instanceof VKConnect) {
                ViewerInfo info = new ViewerInfo(connection);
                synchronized (lock) {
                    toAcknowledge.add(info);
                }
                // Send an OK
                try {
                    connection.sendMessage(new KVConnectOK(worldModel.getAllEntities()));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (msg instanceof VKAcknowledge) {
                if (acknowledge()) {
                    System.out.println("Viewer acknowledged");
                }
                else {
                    System.out.println("Unexpected viewer acknowledge");
                }
            }
        }
    }

    private static class ViewerInfo {
        Connection connection;

        ViewerInfo(Connection c) {
            this.connection = c;
        }
    }
}