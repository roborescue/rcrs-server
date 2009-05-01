package rescuecore2.connection;

import rescuecore2.messages.Message;
import rescuecore2.messages.MessageCodec;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
   Abstract base class for Connection implementations.
 */
public abstract class AbstractConnection implements Connection {
    private List<ConnectionListener> listeners;
    private MessageCodec codec;

    /**
       Construct an abstract connection.
       @param codec The MessageCodec to use for encoding/decoding messages.
    */
    protected AbstractConnection(MessageCodec codec) {
        listeners = new ArrayList<ConnectionListener>();
        this.codec = codec;
    }

    @Override
    public void startup() {}

    @Override
    public void shutdown() {}

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
    public void sendMessage(Message msg) throws IOException {
        // Turn the message into bytes
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(msg, out);
        sendBytes(out.toByteArray());
    }

    @Override
    public void sendMessages(Collection<Message> messages) throws IOException {
        // Turn the messages into bytes
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(messages, out);
        sendBytes(out.toByteArray());
    }

    /**
       Send some bytes to the other end of the connection.
       @param b The bytes to send.
       @throws IOException If the data cannot be sent.
    */
    protected abstract void sendBytes(byte[] b) throws IOException;

    /**
       Process some bytes that were received. The default implementation will use the installed MessageCodec to decode all messages in the buffer and send them to listeners.
       @param b The received bytes.
    */
    protected void bytesReceived(byte[] b) {
        ByteArrayInputStream decode = new ByteArrayInputStream(b);
        Message m = null;
        try {
            do {
                m = codec.decode(decode);
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
}