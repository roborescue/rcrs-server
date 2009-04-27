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
   FIXME: Make writes asynchronous.
 */
public class TCPConnection extends AbstractConnection {
    private InputStream in;
    private OutputStream out;
    private Socket socket;
    private ReadThread readThread;

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
     */
    public TCPConnection(MessageFactory factory, Socket socket) throws IOException {
	super(factory);
	this.socket = socket;
        socket.setSoTimeout(1000);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        readThread = new ReadThread();
    }

    @Override
    public void startup() {
        readThread.start();
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            readThread.kill();
        }
        catch (InterruptedException e) {
            // Log and ignore
            // FIXME: Log it!
        }
        try {
            out.flush();
        }
        catch (IOException e) {
            // Log and ignore
            // FIXME: Log it!
        }
        try {
            out.close();
        }
        catch (IOException e) {
            // Log and ignore
            // FIXME: Log it!
        }
        try {
            in.close();
        }
        catch (IOException e) {
            // Log and ignore
            // FIXME: Log it!
        }
        try {
            socket.close();
        }
        catch (IOException e) {
            // Log and ignore
            // FIXME: Log it!
        }
    }

    @Override
    protected void sendBytes(byte[] b) throws IOException {
        writeInt32(b.length, out);
        out.write(b);
        out.flush();
    }

    /**
       Worker thread that reads from the TCP input stream.
     */
    private class ReadThread extends WorkerThread {
        @Override
        protected boolean work() {
            try {
                //                System.out.println("TCPConnection read thread: waiting for input");
                int size = readInt32(in);
                //                System.out.println("TCPConnection read thread: reading " + size + " bytes");
                if (size > -1) {
                    byte[] buffer = readBytes(size, in);
                    bytesReceived(buffer);
                }
                return true;
            }
            catch (InterruptedIOException e) {
                return false;
            }
            catch (SocketException e) {
                return false;
            }
            catch (IOException e) {
                System.err.println(e);
                return true;
            }
        }
    }
}