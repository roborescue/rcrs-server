package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

/**
   Abstract base class for component implementations.
   @param <T> The subclass of Entity that this agent understands.
 */
public abstract class AbstractComponent<T extends Entity> implements Component {
    private static final int TIMEOUT = 10000;

    /**
       The connection to the kernel.
    */
    protected Connection connection;

    /**
       The world model.
    */
    protected WorldModel<T> model;

    private State connectionState;
    private String failureReason;
    private int connectID;
    private final Object connectLock = new Object();

    /**
       Create a new AbstractComponent.
    */
    protected AbstractComponent() {
        connectionState = State.NOT_CONNECTED;
    }

    @Override
    public final String connect(Connection c, int uniqueID) throws InterruptedException, ConnectionException {
        synchronized (connectLock) {
            ConnectionListener l = createConnectionListener();
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
       Construct the world model.
       @return The world model.
    */
    protected abstract WorldModel<T> createWorldModel();

    /**
       Send a message to the kernel and silently ignore any errors.
       @param msg The message to send.
    */
    protected final void send(Message msg) {
        try {
            connection.sendMessage(msg);
        }
        catch (ConnectionException e) {
            // Ignore and log
            System.out.println(e);
        }
    }

    /**
       Create a message for estabilishing a connection with the kernel.
       @param uniqueID The unique ID to use for the connection attempt.
       @return A Message for connecting to the kernel.
    */
    protected abstract Message createConnectMessage(int uniqueID);

    /**
       Create a ConnectionListener for negotiating the handshake with the kernel.
       @return A ConnectionListener.
    */
    protected abstract ConnectionListener createConnectionListener();

    /**
       Check if a request ID matches this component's connection ID.
       @param id The ID the check.
       @return true Iff the ID matches this component's connection ID.
    */
    protected final boolean checkRequestID(int id) {
        return connectID == id;
    }

    private void connectImpl(int uniqueID) throws InterruptedException, ConnectionException {
        connectID = uniqueID;
        connectionState = State.CONNECTING;
        connection.sendMessage(createConnectMessage(uniqueID));
        // Wait for a reply
        while (connectionState == State.CONNECTING) {
            connectLock.wait(TIMEOUT);
        }
    }

    /**
       Notification that the connection was accepted.
    */
    protected final void connectionSucceeded() {
        synchronized (connectLock) {
            connectionState = State.CONNECTED;
            failureReason = null;
            connectLock.notifyAll();
        }
    }

    /**
       Notification that the connection failed.
       @param reason The reason for failure.
    */
    protected final void connectionFailed(String reason) {
        synchronized (connectLock) {
            connectionState = State.CONNECT_ERROR;
            failureReason = reason;
            connectLock.notifyAll();
        }
    }

    private static enum State {
        NOT_CONNECTED,
        CONNECTING,
        CONNECTED,
        CONNECT_ERROR;
    }
}