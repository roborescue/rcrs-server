package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.io.IOException;

import kernel.ViewerManager;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.WorldModel;

import rescuecore2.version0.entities.RescueObject;
import rescuecore2.version0.messages.VKConnect;
import rescuecore2.version0.messages.VKAcknowledge;
import rescuecore2.version0.messages.KVConnectOK;
import rescuecore2.version0.messages.Update;

/**
   ViewerManager implementation for classic Robocup Rescue.
 */
public class LegacyViewerManager implements ViewerManager<RescueObject> {
    private WorldModel<RescueObject> worldModel;

    private Set<ViewerInfo> toAcknowledge;
    private Set<ViewerInfo> allViewers;

    private final Object lock = new Object();

    /**
       Create a LegacyViewerManager.
    */
    public LegacyViewerManager() {
        toAcknowledge = new HashSet<ViewerInfo>();
        allViewers = new HashSet<ViewerInfo>();
    }

    @Override
    public void setWorldModel(WorldModel<RescueObject> world) {
        worldModel = world;
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
                System.out.println("Waiting for " + toAcknowledge.size() + " viewers");
            }
        }
    }

    @Override
    public void sendToAll(Collection<? extends Message> messages) {
        Collection<ViewerInfo> info = new HashSet<ViewerInfo>();
        synchronized (lock) {
            info.addAll(allViewers);
        }
        for (ViewerInfo next : info) {
            try {
                if (!next.dead) {
                    next.connection.sendMessages(messages);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                next.dead = true;
            }
            catch (ConnectionException e) {
                e.printStackTrace();
                next.dead = true;
            }
        }
    }

    @Override
    public void sendUpdate(int time, Collection<RescueObject> updates) {
        sendToAll(Collections.singleton(new Update(time, updates)));
    }

    @Override
    public void shutdown() {
    }

    private boolean acknowledge(Connection c) {
        synchronized (lock) {
            for (ViewerInfo next : toAcknowledge) {
                if (next.connection == c) {
                    toAcknowledge.remove(next);
                    allViewers.add(next);
                    lock.notifyAll();
                    return true;
                }
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
                System.out.println("Viewer connected");
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
                    info.dead = true;
                }
                catch (ConnectionException e) {
                    e.printStackTrace();
                    info.dead = true;
                }
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

    private static class ViewerInfo {
        Connection connection;
        boolean dead;

        ViewerInfo(Connection c) {
            this.connection = c;
            this.dead = false;
        }
    }
}