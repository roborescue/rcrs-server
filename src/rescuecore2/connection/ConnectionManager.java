package rescuecore2.connection;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.InterruptedIOException;
import java.io.IOException;

import java.util.Set;
import java.util.HashSet;

import rescuecore2.misc.WorkerThread;
import rescuecore2.messages.MessageCodec;

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
    public void listen(int port, MessageCodec codec, ConnectionManagerListener listener) throws IOException {
	ServerSocket socket = new ServerSocket(port);
	Reader r = new Reader(socket, codec, listener);
	readers.add(r);
	r.start();
    }

    private class Reader extends WorkerThread {
	private ServerSocket socket;
	private MessageCodec codec;
	private ConnectionManagerListener callback;

	public Reader(ServerSocket socket, MessageCodec codec, ConnectionManagerListener callback) {
	    this.socket = socket;
	    this.codec = codec;
	    this.callback = callback;
	}

	@Override
	protected boolean work() {
	    try {
		Socket s = socket.accept();
		TCPConnection conn = new TCPConnection(codec, s);
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