package kernel.legacy;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import kernel.AbstractViewerManager;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;

import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.messages.VKConnect;
import rescuecore2.version0.messages.VKAcknowledge;
import rescuecore2.version0.messages.KVConnectOK;
import rescuecore2.version0.messages.Update;
import rescuecore2.version0.messages.AgentCommand;
import rescuecore2.version0.messages.Commands;

/**
   ViewerManager implementation for classic Robocup Rescue.
 */
public class LegacyViewerManager extends AbstractViewerManager<RescueEntity, IndexedWorldModel> {
    private Set<Viewer> toAcknowledge;

    /**
       Create a LegacyViewerManager.
    */
    public LegacyViewerManager() {
        toAcknowledge = new HashSet<Viewer>();
    }

    @Override
    public void newConnection(Connection c) {
        c.addConnectionListener(new ViewerConnectionListener());
    }

    @Override
    public void waitForViewers() throws InterruptedException {
        synchronized (toAcknowledge) {
            while (!toAcknowledge.isEmpty()) {
                toAcknowledge.wait(1000);
                System.out.println("Waiting for " + toAcknowledge.size() + " viewers");
            }
        }
    }

    @Override
    public void sendUpdate(int time, Collection<RescueEntity> updates) {
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
        synchronized (toAcknowledge) {
            for (Viewer next : toAcknowledge) {
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
                Viewer data = new Viewer(connection);
                synchronized (toAcknowledge) {
                    toAcknowledge.add(data);
                }
                // Send an OK
                data.send(Collections.singleton(new KVConnectOK(getWorldModel().getAllEntities())));
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