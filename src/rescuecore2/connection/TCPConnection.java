package rescuecore2.connection;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readBytes;

import java.net.Socket;
import java.net.SocketException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.InterruptedIOException;

import rescuecore2.messages.MessageFactory;
import rescuecore2.misc.WorkerThread;

/**
   TCP implementation of a Connection.
 */
public class TCPConnection extends StreamConnection {
    private Socket socket;
    private String name;

    /**
       Make a connection to the local host on a given port.
       @param factory The MessageFactory to use for creating messages.
       @param port The port to connect to.
       @throws IOException If the host cannot be contacted.
    */
    public TCPConnection(MessageFactory factory, int port) throws IOException {
        this(factory, null, port);
    }

    /**
       Make a connection to a specific host on a given port.
       @param factory The MessageFactory to use for creating messages.
       @param address The address of the host.
       @param port The port to connect to.
       @throws IOException If the host cannot be contacted.
    */
    public TCPConnection(MessageFactory factory, String address, int port) throws IOException {
        this(factory, new Socket(address, port));
    }

    /**
       Create a TCPConnection from an existing socket.
       @param factory The MessageFactory to use for creating messages.
       @param socket The socket to attach to.
       @throws IOException If there is a problem opening the streams.
    */
    public TCPConnection(MessageFactory factory, Socket socket) throws IOException {
        super(factory, socket.getInputStream(), socket.getOutputStream());
        this.socket = socket;
        socket.setSoTimeout(1000);
        name = "TCPConnection: local port " + socket.getLocalPort() + ", endpoint = " + socket.getInetAddress() + ":" + socket.getPort();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    protected void shutdownImpl() {
        super.shutdownImpl();
        try {
            socket.close();
        }
        catch (IOException e) {
            // Log and ignore
            // FIXME: Log it!
        }
    }
}