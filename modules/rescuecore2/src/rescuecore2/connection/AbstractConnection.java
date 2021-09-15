package rescuecore2.connection;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readMessage;
import static rescuecore2.misc.EncodingTools.writeMessage;

import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.messages.protobuf.MsgProtoBuf;
import rescuecore2.misc.WorkerThread;
import rescuecore2.registry.Registry;
import rescuecore2.log.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
   Abstract base class for Connection implementations.
 */
public abstract class AbstractConnection implements Connection {
    private static final int BROADCAST_WAIT = 10000;

    private List<ConnectionListener> listeners;
    private List<Message> toSend;
    private MessageBroadcastThread broadcast;
    private Registry registry;

    private boolean logBytes;
    private String name;

    private volatile State state;

    private final Object stateLock = new Object();

    /**
       Construct an abstract connection.
    */
    protected AbstractConnection() {
        listeners = new ArrayList<ConnectionListener>();
        toSend = new LinkedList<Message>();
        logBytes = false;
        state = State.NOT_STARTED;
        registry = Registry.SYSTEM_REGISTRY;
        name = toString();
    }

    @Override
    public void setLogBytes(boolean enabled) {
        logBytes = enabled;
    }

    @Override
    public final void startup() {
        synchronized (stateLock) {
            if (state == State.NOT_STARTED) {
                Registry old = Registry.getCurrentRegistry();
                Registry.setCurrentRegistry(registry);
                try {
                    broadcast = new MessageBroadcastThread();
                    broadcast.start();
                    startupImpl();
                    state = State.STARTED;
                }
                finally {
                    Registry.setCurrentRegistry(old);
                }
            }
        }
    }

    @Override
    public final void shutdown() {
        synchronized (stateLock) {
            if (state == State.STARTED) {
                try {
                    broadcast.kill();
                }
                catch (InterruptedException e) {
                    Logger.error("AbstractConnection interrupted while shutting down broadcast thread", e);
                }
                shutdownImpl();
                state = State.SHUTDOWN;
            }
        }
    }

    @Override
    public boolean isAlive() {
        boolean result;
        synchronized (stateLock) {
            result = state == State.STARTED;
        }
        return result;
    }

    @Override
    public void addConnectionListener(ConnectionListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeConnectionListener(ConnectionListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    @Override
    public void sendMessage(Message msg) throws ConnectionException {
        if (msg == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        sendMessages(Collections.singleton(msg));
    }

    @Override
    public void sendMessages(Collection<? extends Message> messages) throws ConnectionException {
        if (messages == null) {
            throw new IllegalArgumentException("Messages cannot be null");
        }
        synchronized (stateLock) {
            if (state == State.NOT_STARTED) {
                throw new ConnectionException("Connection has not been started");
            }
            if (state == State.SHUTDOWN) {
                throw new ConnectionException("Connection has been shut down");
            }
            if (!isAlive()) {
                throw new ConnectionException("Connection is dead");
            }
        }
        try {
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            for (Message next : messages) {
//                writeMessage(next, out);
//            }
//            // Add a zero to indicate no more messages
//            writeInt32(0, out);
//            // Send the bytes
//            if (logBytes) {
//                ByteLogger.log(out.toByteArray());
//            }
//            sendBytes(out.toByteArray());
      	  for ( Message msg : messages ) {
//      	    builder.addMessages(MsgProtoBuf.setMessageProto(msg));
      	    sendMessageProto( msg.toMessageProto());
      	  }
        }
        catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    @Override
    public void setRegistry(Registry r) {
        this.registry = r;
    }

    @Override
    public Registry getRegistry() {
        return registry;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        this.name = newName;
    }

    @Override
    public String toString() {
        if (name == null) {
            return super.toString();
        }
        return name;
    }

    /**
       Send some bytes to the other end of the connection.
       @param b The bytes to send.
       @throws IOException If the data cannot be sent.
    */
//    protected abstract void sendBytes(byte[] b) throws IOException;
    protected abstract void sendMessageProto(MessageProto  messageProto) throws IOException;

    /**
       Perform startup actions. This will only ever be called once.
    */
    protected abstract void startupImpl();

    /**
       Perform shutdown actions. This will only ever be called once.
    */
    protected abstract void shutdownImpl();

    /**
       Process some bytes that were received. The default implementation will use the Registry to decode all messages in the buffer and send them to listeners.
       @param b The received bytes.
    */
//    protected void bytesReceived(byte[] b) {
//        InputStream decode = new ByteArrayInputStream(b);
//        Message m = null;
//        try {
//            do {
//                m = readMessage(decode);
//                if (m != null) {
//                    fireMessageReceived(m);
//                }
//            } while (m != null);
//        }
//        catch (IOException e) {
//            Logger.error("AbstractConnection error reading message", e);
//            ByteLogger.log(b, this.toString());
//        }
//        // CHECKSTYLE:OFF:IllegalCatch
//        catch (RuntimeException e) {
//            Logger.error("AbstractConnection error reading message", e);
//            ByteLogger.log(b, this.toString());
//            throw e;
//        }
//        catch (Error e) {
//            Logger.error("AbstractConnection error reading message", e);
//            ByteLogger.log(b, this.toString());
//            throw e;
//        }
//        // CHECKSTYLE:ON:IllegalCatch
//    }
    protected void messageProtoReceived(MessageProto messageProto) {
		  fireMessageReceived(MsgProtoBuf.messageProto2Message(messageProto));
}
    /**
       Fire a messageReceived event to all registered listeners.
       @param m The message that was received.
    */
    protected void fireMessageReceived(Message m) {
        synchronized (toSend) {
            toSend.add(m);
            toSend.notifyAll();
        }
    }

    /**
       The state of this connection: either not yet started, started or shut down.
    */
    protected enum State {
        /** CHECKSTYLE:OFF:JavadocVariableCheck. */
        NOT_STARTED,
        STARTED,
        SHUTDOWN;
        /** CHECKSTYLE:ON:JavadocVariableCheck. */
    }

    /**
       Worker thread that broadcasts messages to listeners.
    */
    private class MessageBroadcastThread extends WorkerThread {
        @Override
        protected boolean work() throws InterruptedException {
            Message m = null;
            synchronized (toSend) {
                if (toSend.isEmpty()) {
                    toSend.wait(BROADCAST_WAIT);
                    return true;
                }
                else {
                    m = toSend.remove(0);
                }
            }
            if (m == null) {
                return true;
            }
            ConnectionListener[] l;
            synchronized (listeners) {
                l = new ConnectionListener[listeners.size()];
                listeners.toArray(l);
            }
            for (ConnectionListener next : l) {
                next.messageReceived(AbstractConnection.this, m);
            }
            return true;
        }
    }
}
