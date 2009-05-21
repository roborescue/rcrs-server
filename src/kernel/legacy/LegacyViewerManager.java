package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.io.IOException;

import kernel.AbstractViewerManager;
import kernel.ViewerInfo;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;

import rescuecore2.version0.entities.RescueObject;
import rescuecore2.version0.messages.VKConnect;
import rescuecore2.version0.messages.VKAcknowledge;
import rescuecore2.version0.messages.KVConnectOK;
import rescuecore2.version0.messages.Update;
import rescuecore2.version0.messages.AgentCommand;
import rescuecore2.version0.messages.Commands;

/**
   ViewerManager implementation for classic Robocup Rescue.
 */
public class LegacyViewerManager extends AbstractViewerManager<RescueObject, IndexedWorldModel> {
    private IndexedWorldModel worldModel;

    private Set<ViewerData> toAcknowledge;
    private Set<ViewerData> allViewers;

    private final Object lock = new Object();

    /**
       Create a LegacyViewerManager.
    */
    public LegacyViewerManager() {
        toAcknowledge = new HashSet<ViewerData>();
        allViewers = new HashSet<ViewerData>();
    }

    @Override
    public void setWorldModel(IndexedWorldModel world) {
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
        Collection<ViewerData> data = new HashSet<ViewerData>();
        synchronized (lock) {
            data.addAll(allViewers);
        }
        for (ViewerData next : data) {
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
    public void sendAgentCommands(int time, Collection<? extends Message> commands) {
        Collection<AgentCommand> agentCommands = new ArrayList<AgentCommand>();
        for (Message next : commands) {
            if (next instanceof AgentCommand) {
                agentCommands.add((AgentCommand)next);
            }
        }
        sendToAll(Collections.singleton(new Commands(time, agentCommands)));
    }

    @Override
    public void shutdown() {
    }

    private boolean acknowledge(Connection c) {
        synchronized (lock) {
            for (ViewerData next : toAcknowledge) {
                if (next.connection == c) {
                    toAcknowledge.remove(next);
                    allViewers.add(next);
                    lock.notifyAll();
                    fireViewerConnected(new ViewerInfo(next.connection.toString()));
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
                ViewerData data = new ViewerData(connection);
                synchronized (lock) {
                    toAcknowledge.add(data);
                }
                // Send an OK
                try {
                    connection.sendMessage(new KVConnectOK(worldModel.getAllEntities()));
                }
                catch (IOException e) {
                    e.printStackTrace();
                    data.dead = true;
                }
                catch (ConnectionException e) {
                    e.printStackTrace();
                    data.dead = true;
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

    private static class ViewerData {
        Connection connection;
        boolean dead;

        ViewerData(Connection c) {
            this.connection = c;
            this.dead = false;
        }
    }
}