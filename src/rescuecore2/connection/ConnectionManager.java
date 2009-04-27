package rescuecore2.connection;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.InterruptedIOException;
import java.io.IOException;

import java.util.Set;
import java.util.HashSet;

import rescuecore2.misc.WorkerThread;
import rescuecore2.messages.MessageFactory;

/**
   A class for managing incoming connections.
 */
public class ConnectionManager {
    private Set<Reader> readers;

    public ConnectionManager() {
	readers = new HashSet<Reader>();
    }

    /**
       Listen for connections on a particular port.
       @param port The port to listen on.
     */
    public void listen(int port, MessageFactory factory, ConnectionManagerListener listener) throws IOException {
        System.out.println("Listening for connections on port " + port);
	ServerSocket socket = new ServerSocket(port);
        socket.setSoTimeout(1000);
	Reader r = new Reader(socket, factory, listener);
	readers.add(r);
	r.start();
    }

    /**
       Shut down this ConnectionManager.
    */
    public void shutdown() {
        for (Reader next : readers) {
            try {
                next.kill();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                // FIXME: Log it!
            }
        }
    }

    private static class Reader extends WorkerThread {
	private ServerSocket socket;
	private MessageFactory factory;
	private ConnectionManagerListener callback;

	public Reader(ServerSocket socket, MessageFactory factory, ConnectionManagerListener callback) {
	    this.socket = socket;
	    this.factory = factory;
	    this.callback = callback;
	}

	@Override
	protected boolean work() {
	    try {
		Socket s = socket.accept();
		TCPConnection conn = new TCPConnection(factory, s);
		callback.newConnection(conn);
	    }
	    catch (InterruptedIOException e) {
		// Ignore
	    }
            catch (IOException e) {
                e.printStackTrace();
                // FIXME: Log it!
            }
            return true;
	}

	@Override
	protected void cleanup() {
	    try {
		socket.close();
	    }
	    catch (IOException e) {
		e.printStackTrace();
		// FIXME: Log it!
	    }
	}
    }
}