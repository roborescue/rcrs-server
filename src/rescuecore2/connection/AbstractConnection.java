package rescuecore2.connection;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readBytes;

import rescuecore2.messages.Message;
import rescuecore2.messages.MessageFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
   Abstract base class for Connection implementations.
 */
public abstract class AbstractConnection implements Connection {
    private List<ConnectionListener> listeners;
    private MessageFactory factory;

    private boolean logBytes;

    private volatile State state;

    private final Object stateLock = new Object();
    private final Object factoryLock = new Object();

    /**
       Construct an abstract connection.
       @param factory The factory class used to generate new messages.
    */
    protected AbstractConnection(MessageFactory factory) {
        listeners = new ArrayList<ConnectionListener>();
        this.factory = factory;
        logBytes = false;
	state = State.NOT_STARTED;
    }

    @Override
    public void setLogBytes(boolean enabled) {
        logBytes = enabled;
    }

    @Override
    public void setMessageFactory(MessageFactory newFactory) {
	if (newFactory == null) {
	    throw new IllegalArgumentException("Message factory cannot be null");
	}
        synchronized (factoryLock) {
            this.factory = newFactory;
        }
    }

    @Override
    public final void startup() {
        synchronized (stateLock) {
            if (state == State.NOT_STARTED) {
                startupImpl();
                state = State.STARTED;
            }
        }
    }

    @Override
    public final void shutdown() {
        synchronized (stateLock) {
            if (state == State.STARTED) {
                shutdownImpl();
                state = State.SHUTDOWN;
            }
        }
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
    public void sendMessage(Message msg) throws IOException, ConnectionException {
	if (msg == null) {
	    throw new IllegalArgumentException("Message cannot be null");
	}
        sendMessages(Collections.singleton(msg));
    }

    @Override
    public void sendMessages(Collection<Message> messages) throws IOException, ConnectionException {
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
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (Message next : messages) {
            encodeMessage(next, out);
        }
        // Add a zero to indicate no more messages
        writeInt32(0, out);
        // Send the bytes
        if (logBytes) {
            ByteLogger.log(out.toByteArray());
        }
        sendBytes(out.toByteArray());
    }

    /**
       Send some bytes to the other end of the connection.
       @param b The bytes to send.
       @throws IOException If the data cannot be sent.
    */
    protected abstract void sendBytes(byte[] b) throws IOException;

    /**
       Perform startup actions. This will only ever be called once.
    */
    protected abstract void startupImpl();

    /**
       Perform shutdown actions. This will only ever be called once.
    */
    protected abstract void shutdownImpl();

    /**
       Process some bytes that were received. The default implementation will use the installed MessageCodec to decode all messages in the buffer and send them to listeners.
       @param b The received bytes.
    */
    protected void bytesReceived(byte[] b) {
        ByteArrayInputStream decode = new ByteArrayInputStream(b);
        Message m = null;
        try {
            do {
                // Take a copy of the message factory reference in case someone tries to change the factory while we're decoding the message
                MessageFactory f;
                synchronized (factoryLock) {
                    f = factory;
                }
                m = decodeMessage(decode, f);
                if (m != null) {
                    fireMessageReceived(m);
                }
            } while (m != null);
        }
        catch (IOException e) {
            // Log and ignore
            // FIXME: Log it!
            System.err.println(e);
        }
    }

    /**
       Fire a messageReceived event to all registered listeners.
       @param m The message that was received.
    */
    protected void fireMessageReceived(Message m) {
        ConnectionListener[] l;
        synchronized (listeners) {
            l = new ConnectionListener[listeners.size()];
            listeners.toArray(l);
        }
        for (ConnectionListener next : l) {
            next.messageReceived(m);
        }
    }

    private void encodeMessage(Message msg, OutputStream out) throws IOException {
        //        System.out.println(this + ": Sending message: " + msg);
        // Turn the message into bytes
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	msg.write(bytes);
        // Write the header then the message body
        writeInt32(msg.getMessageTypeID(), out);
        writeInt32(bytes.size(), out);
        out.write(bytes.toByteArray());
    }

    private Message decodeMessage(InputStream in, MessageFactory factory) throws IOException {
        int id = readInt32(in);
        if (id == 0) {
            return null;
        }
        int size = readInt32(in);
        byte[] data = readBytes(size, in);
        Message result = factory.createMessage(id);
        // Read all the message components
        InputStream input = new ByteArrayInputStream(data);
	result.read(input);
        //        System.out.println(this + ": Received message: " + result);
        return result;
    }

    protected enum State {
	NOT_STARTED,
            STARTED,
            SHUTDOWN;
    }
}