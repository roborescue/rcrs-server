package rescuecore2.connection;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readBytes;

import rescuecore2.messages.Message;
import rescuecore2.messages.MessageComponent;
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

    /**
       Construct an abstract connection.
       @param factory The factory class used to generate new messages.
    */
    protected AbstractConnection(MessageFactory factory) {
        listeners = new ArrayList<ConnectionListener>();
        this.factory = factory;
        logBytes = false;
    }

    @Override
    public void setLogBytes(boolean enabled) {
        logBytes = enabled;
    }

    @Override
    public void setMessageFactory(MessageFactory factory) {
        this.factory = factory;
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
        sendMessages(Collections.singleton(msg));
    }

    @Override
    public void sendMessages(Collection<Message> messages) throws IOException {
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
       Process some bytes that were received. The default implementation will use the installed MessageCodec to decode all messages in the buffer and send them to listeners.
       @param b The received bytes.
    */
    protected void bytesReceived(byte[] b) {
        ByteArrayInputStream decode = new ByteArrayInputStream(b);
        Message m = null;
        try {
            do {
                m = decodeMessage(decode);
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
        System.out.println("Sending message: " + msg);
        // Turn the message into bytes
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (MessageComponent next : msg.getComponents()) {
            next.write(bytes);
        }
        // Write the header then the message body
        writeInt32(msg.getMessageTypeID(), out);
        writeInt32(bytes.size(), out);
        out.write(bytes.toByteArray());
    }

    private Message decodeMessage(InputStream in) throws IOException {
        int id = readInt32(in);
        if (id == 0) {
            return null;
        }
        int size = readInt32(in);
        byte[] data = readBytes(size, in);
        Message result = factory.createMessage(id);
        // Read all the message components
        InputStream input = new ByteArrayInputStream(data);
        for (MessageComponent next : result.getComponents()) {
            next.read(input);
        }
        System.out.println("Received message: " + result);
        return result;
    }
}