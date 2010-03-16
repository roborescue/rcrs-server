package rescuecore2.connection;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import rescuecore2.messages.Message;
import rescuecore2.misc.Pair;

public class TCPConnectionTest extends ConnectionTestCommon {
    private static final int SERVER_PORT = 19243;

    private ServerSocket server;

    @Before
    @Override
    public void setup() throws IOException {
	server = new ServerSocket(SERVER_PORT);
	server.setSoTimeout(10000);
	super.setup();
    }

    @After
    public void shutdown() throws IOException {
	server.close();
    }

    @Override
    protected Pair<Connection, Connection> makeConnectionPair() throws IOException {
	Connection client = new TCPConnection("localhost", SERVER_PORT);
	Connection server = new TCPConnection(assertIncomingConnection());
	return new Pair<Connection, Connection>(client, server);
    }

    @Test
    public void testConnectToPort() throws IOException {
	TCPConnection c = new TCPConnection(SERVER_PORT);
	assertIncomingConnection();
    }

    @Test
    public void testConnectToHostAndPort() throws IOException {
	TCPConnection c = new TCPConnection("localhost", SERVER_PORT);
	assertIncomingConnection();
    }

    @Test
    public void testName() throws IOException {
        Socket socket = new Socket("localhost", SERVER_PORT);
	TCPConnection c = new TCPConnection(socket);
	assertIncomingConnection();
        assertEquals("TCPConnection: local port " + socket.getLocalPort() + ", endpoint = " + socket.getInetAddress() + ":" + socket.getPort(), c.toString());
    }

    @Test
    public void testShutdownIOException() throws IOException {
        Socket socket = new Socket("localhost", SERVER_PORT) {
                public void close() throws IOException {
                    throw new IOException("Socket close failed");
                }
            };
        TCPConnection c = new TCPConnection(socket);
        assertIncomingConnection();
        c.startup();
        c.shutdown();
    }

    private Socket assertIncomingConnection() throws IOException {
	Socket serverSocket = server.accept();
	if (serverSocket == null) {
	    fail("No connection made");
	}
	return serverSocket;
    }
}
