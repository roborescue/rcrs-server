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
import rescuecore2.messages.MessageFactory;
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
    protected Pair<Connection, Connection> makeConnectionPair(MessageFactory factory) throws IOException {
	Connection client = new TCPConnection(factory, "localhost", SERVER_PORT);
	Connection server = new TCPConnection(factory, assertIncomingConnection());
	return new Pair<Connection, Connection>(client, server);
    }

    @Test
    public void testConnectToPort() throws IOException {
	TCPConnection c = new TCPConnection(factory, SERVER_PORT);
	assertIncomingConnection();
    }

    @Test
    public void testConnectToHostAndPort() throws IOException {
	TCPConnection c = new TCPConnection(factory, "localhost", SERVER_PORT);
	assertIncomingConnection();
    }

    private Socket assertIncomingConnection() throws IOException {
	Socket serverSocket = server.accept();
	if (serverSocket == null) {
	    fail("No connection made");
	}
	return serverSocket;
    }

    /*
    @Override
    protected byte[] encodeMessages(Message... messages) throws IOException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	for (Message next : messages) {
	    writeInt32(next.getMessageTypeID(), out);
	    ByteArrayOutputStream body = new ByteArrayOutputStream();
	    next.write(body);
	    byte[] bytes = body.toByteArray();
	    writeInt32(bytes.length, out);
	    out.write(bytes);
	}
	writeInt32(0, out);
	return out.toByteArray();
    }

    @Override
    protected Message decodeMessages(byte[] in) {
	
    }
    */
}