package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.AKConnect;
import rescuecore2.messages.control.AKAcknowledge;
import rescuecore2.messages.control.KAConnectOK;
import rescuecore2.messages.control.KAConnectError;
import rescuecore2.messages.control.KASense;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

import java.util.List;
import java.util.ArrayList;

/**
   Abstract base class for agent implementations.
   @param <T> The subclass of Entity that this agent understands.
 */
public abstract class AbstractAgent<T extends Entity> implements Agent {
    private static final int TIMEOUT = 10000;

    /**
       The connection to the kernel.
     */
    protected Connection connection;

    /**
       The world model.
     */
    protected WorldModel<T> model;

    /**
       The ID of the entity controlled by this agent.
     */
    protected EntityID entityID;

    private State connectionState;
    private String failureReason;
    private int connectID;
    private final Object connectLock = new Object();

    /**
       Create a new AbstractAgent.
     */
    protected AbstractAgent() {
        connectionState = State.NOT_CONNECTED;
    }

    @Override
    public final String connect(Connection c, int uniqueID) throws InterruptedException, ConnectionException {
        synchronized (connectLock) {
            AgentConnectionListener l = new AgentConnectionListener();
            if (connectionState == State.NOT_CONNECTED) {
                connection = c;
                model = createWorldModel();
                c.addConnectionListener(l);
                connectImpl(uniqueID);
            }
            while (connectionState == State.CONNECTING) {
                // Wait until the state changes
                connectLock.wait(TIMEOUT);
            }
            if (failureReason == null) {
                postConnect();
            }
            else {
                c.removeConnectionListener(l);
            }
            return failureReason;
        }
    }


    /**
       Notification that the connection to the kernel was successful. The default implementation does nothing.
     */
    protected void postConnect() {}

    /**
       Notification that a timestep has started.
       @param time The timestep.
       @param changed A list of entities that changed this timestep.
     */
    protected abstract void think(int time, List<EntityID> changed);

    /**
       Construct the world model.
       @return The world model.
     */
    protected abstract WorldModel<T> createWorldModel();

    /**
       Get the list of entity IDs that this agent is willing to control.
       @return An array of entity IDs.
    */
    protected abstract int[] getRequestedEntityIDs();

    /**
       Process an incoming sense message. This will be called after the world model has been updated. The default implementation calls {@link #think}. Subclasses should generally not override this method but instead implement the {@link #think} method.
       @param sense The sense message.
     */
    protected void processSense(KASense sense) {
        List<Entity> updates = sense.getUpdates();
        //        System.out.println("Agent " + entityID + " received " + updates.size() + " updates");
        List<EntityID> changed = new ArrayList<EntityID>(updates.size());
        for (Entity next : updates) {
            changed.add(next.getID());
        }
        think(sense.getTime(), changed);
    }

    /**
       Get the entity controlled by this agent.
       @return The entity controlled by this agent.
     */
    protected T me() {
        if (entityID == null) {
            return null;
        }
        return model.getEntity(entityID);
    }

    /**
       Send a command to the kernel and silently ignore any errors.
       @param cmd The command to send.
    */
    protected void send(Command cmd) {
        try {
            connection.sendMessage(cmd);
        }
        catch (ConnectionException e) {
            // Ignore and log
            System.out.println(e);
        }
    }

    private void connectImpl(int uniqueID) throws InterruptedException, ConnectionException {
        connectID = uniqueID;
        connectionState = State.CONNECTING;
        connection.sendMessage(new AKConnect(uniqueID, 0, getRequestedEntityIDs()));
        // Wait for a reply
        while (connectionState == State.CONNECTING) {
            connectLock.wait(TIMEOUT);
        }
    }

    private void handleSense(KASense sense) {
        if (!entityID.equals(sense.getAgentID())) {
            return;
        }
        //        System.out.println("Agent " + me() + " received " + sense);
        model.merge(sense.getUpdates());
        processSense(sense);
    }

    private void handleConnectOK(KAConnectOK ok) {
        if (connectID != ok.getRequestID()) {
            return;
        }
        //        System.out.println("Agent connected OK: " + ok);
        model.removeAllEntities();
        model.merge(ok.getEntities());
        entityID = ok.getAgentID();
        // Send an acknowledge
        try {
            connection.sendMessage(new AKAcknowledge(ok.getRequestID(), entityID));
            synchronized (connectLock) {
                connectionState = State.CONNECTED;
                failureReason = null;
                connectLock.notifyAll();
            }
        }
        catch (ConnectionException e) {
            e.printStackTrace();
            synchronized (connectLock) {
                connectionState = State.CONNECT_ERROR;
                failureReason = e.toString();
                connectLock.notifyAll();
            }
        }
    }

    private void handleConnectError(KAConnectError error) {
        if (connectID != error.getRequestID()) {
            return;
        }
        //        System.out.println("Error connecting agent: " + error);
        synchronized (connectLock) {
            connectionState = State.CONNECT_ERROR;
            failureReason = error.getReason();
            connectLock.notifyAll();
        }
    }

    private class AgentConnectionListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection c, Message msg) {
            if (msg instanceof KASense) {
                handleSense((KASense)msg);
            }
            if (msg instanceof KAConnectOK) {
                handleConnectOK((KAConnectOK)msg);
            }
            if (msg instanceof KAConnectError) {
                handleConnectError((KAConnectError)msg);
            }
        }
    }

    private static enum State {
        NOT_CONNECTED,
        CONNECTING,
        CONNECTED,
        CONNECT_ERROR;
    }
}