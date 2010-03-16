package rescuecore2.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import rescuecore2.messages.Message;
import rescuecore2.registry.Registry;
import rescuecore2.registry.MessageFactory;
import rescuecore2.misc.Pair;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.Arrays;

public abstract class ConnectionTestCommon {
    private Connection client;
    private Connection server;
    private TestConnectionListener clientListener;
    private TestConnectionListener serverListener;
    private TestMessageFactory factory;
    protected Registry registry;

    protected static final int DELAY = 1000;
    protected static final int TIMEOUT = 3000;

    private static final String MESSAGE_ID_1 = "Test message 1";
    private static final String MESSAGE_ID_2 = "Test message 2";
    private static final String MESSAGE_ID_3 = "Test message 3";

    private final static String FACTORY_1_NAME = "Factory 1";
    private final static String FACTORY_2_NAME = "Factory 2";
    private final static String FACTORY_3_NAME = "Factory 3";

    @Before
    public void setup() throws IOException {
        registry = new Registry();
        registry.registerMessageFactory(new TestMessageFactory(FACTORY_1_NAME, MESSAGE_ID_1));
	Pair<Connection, Connection> connections = makeConnectionPair();
	client = connections.first();
	server = connections.second();
        client.setRegistry(registry);
        server.setRegistry(registry);
	clientListener = new TestConnectionListener();
	serverListener = new TestConnectionListener();
	client.addConnectionListener(clientListener);
	server.addConnectionListener(serverListener);
    }

    /**
       Get two connection objects that represent the two end-points of a communication channel.
    */
    protected abstract Pair<Connection, Connection> makeConnectionPair() throws IOException;

    @Test
    public void testNoMessagesReceivedBeforeStartup() throws IOException, InterruptedException, ConnectionException {
	// Send a message from the client
	client.startup();
	Message m = new TestMessage(MESSAGE_ID_1);
	client.sendMessage(m);
	// Wait a bit
	Thread.sleep(DELAY);
	assertEquals(0, serverListener.getMessageCount());
	// Start the server connection
	server.startup();
        serverListener.waitForMessages(1, TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
    }

    @Test
    public void testNoMessagesAfterShutdown() throws IOException, InterruptedException, ConnectionException {
	// Send a message from the client
	client.startup();
	server.startup();
	Message m = new TestMessage(MESSAGE_ID_1);
	client.sendMessage(m);
	// Wait for a message
        serverListener.waitForMessages(1, TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
	// Shutdown the server connection
	server.shutdown();
	// Send another message and check that it didn't arrive
	try {
	    client.sendMessage(m);
	}
	catch (ConnectionException e) {
            // Could reasonably expect a ConnectionException right now: the server is gone so the client might have shut itself down
        }
	Thread.sleep(DELAY);
	assertEquals(1, serverListener.getMessageCount());
    }

    @Test
    public void testIsAlive() throws InterruptedException {
        assertFalse(client.isAlive());
        client.startup();
        assertTrue(client.isAlive());
        client.shutdown();
        assertFalse(client.isAlive());
    }

    @Test
    public void testRemoveConnectionListener() throws IOException, InterruptedException, ConnectionException {
	client.startup();
	server.startup();
	// Send a message from the client
	Message m = new TestMessage(MESSAGE_ID_1);
	client.sendMessage(m);
	// Wait for a message
        serverListener.waitForMessages(1, TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
	// Remove the listener
	server.removeConnectionListener(serverListener);
	// Send another message
	client.sendMessage(m);
	// Wait a bit
	Thread.sleep(DELAY);
	// Check that the new message didn't arrive
	assertEquals(1, serverListener.getMessageCount());
    }

    @Test
    public void testMultipleStartup() throws IOException, InterruptedException, ConnectionException {
        client.startup();
        client.startup();
        server.startup();
        // Send a message from the client
        Message m = new TestMessage(MESSAGE_ID_1);
        client.sendMessage(m);
        // Wait for a message
        serverListener.waitForMessages(1, TIMEOUT);
        assertEquals(1, serverListener.getMessageCount());
        client.startup();
        client.sendMessage(m);
        // Wait for a message
        serverListener.waitForMessages(2, TIMEOUT);
        assertEquals(2, serverListener.getMessageCount());
    }

    @Test
    public void testMultipleShutdown() throws IOException, InterruptedException, ConnectionException {
	client.startup();
	server.startup();
	// Send a message from the client
	Message m = new TestMessage(MESSAGE_ID_1);
	client.sendMessage(m);
	// Wait for a message
        serverListener.waitForMessages(1, TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
	server.shutdown();
	// Send another message
	try {
	    client.sendMessage(m);
	}
	catch (ConnectionException e) {
	    // Could reasonably expect a ConnectionException right now: the server is gone so the client might have shut itself down.
	}
	// Wait a bit
	Thread.sleep(DELAY);
	// Check that the new message didn't arrive
	assertEquals(1, serverListener.getMessageCount());
	server.shutdown();
	// Send another message
	try {
	    client.sendMessage(m);
	}
	catch (ConnectionException e) {
	    // Could reasonably expect a ConnectionException right now: the server is gone so the client might have shut itself down.
        }
	// Wait a bit
	Thread.sleep(DELAY);
	// Check that the new message didn't arrive
	assertEquals(1, serverListener.getMessageCount());
    }

    @Test
    public void testSendMessage() throws IOException, InterruptedException, ConnectionException {
	client.startup();
	server.startup();
	Message m = new TestMessage(MESSAGE_ID_1);
	client.sendMessage(m);
        serverListener.waitForMessages(1, TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
	assertEquals(m, serverListener.getMessage(0));
    }

    @Test
    public void testSendMessages() throws InterruptedException, ConnectionException{
	client.startup();
	server.startup();
	Message m1 = new TestMessage(MESSAGE_ID_1, "", 3);
	Message m2 = new TestMessage(MESSAGE_ID_1, "", 4, 5);
	client.sendMessages(Arrays.asList(m1, m2));
        serverListener.waitForMessages(2, TIMEOUT);
	assertEquals(2, serverListener.getMessageCount());
	assertEquals(m1, serverListener.getMessage(0));
	assertEquals(m2, serverListener.getMessage(1));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSendNullMessage() throws InterruptedException, ConnectionException {
	client.startup();
	server.startup();
	client.sendMessage(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSendNullMessages() throws InterruptedException, ConnectionException {
	client.startup();
	server.startup();
	client.sendMessages(null);
    }

    @Test(expected=rescuecore2.connection.ConnectionException.class)
    public void testSendMessageBeforeStartup() throws IOException, InterruptedException, ConnectionException {
	server.startup();
	Message m = new TestMessage(MESSAGE_ID_1);
	client.sendMessage(m);
    }

    @Test(expected=rescuecore2.connection.ConnectionException.class)
    public void testSendMessageAfterShutdown() throws IOException, InterruptedException, ConnectionException {
	server.startup();
        client.startup();
	Message m = new TestMessage(MESSAGE_ID_1);
	client.sendMessage(m);
        serverListener.waitForMessages(1, TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
	client.shutdown();
	client.sendMessage(m);
    }

    @Test
    public void testRegisterNewMessageFactory() throws IOException, InterruptedException, ConnectionException {
	client.startup();
	server.startup();
	Message m1 = new TestMessage(MESSAGE_ID_1);
	client.sendMessage(m1);
        serverListener.waitForMessages(1, TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
	assertEquals(m1, serverListener.getMessage(0));
        assertEquals(FACTORY_1_NAME, ((TestMessage)serverListener.getMessage(0)).getDescription());
        registry.registerMessageFactory(new TestMessageFactory(FACTORY_2_NAME, MESSAGE_ID_2));
        Message m2 = new TestMessage(MESSAGE_ID_2);
	client.sendMessage(m2);
	// Check that the second message was interpreted by the new message factory and that the old message is still OK.
        serverListener.waitForMessages(2, TIMEOUT);
	assertEquals(2, serverListener.getMessageCount());
	assertEquals(MESSAGE_ID_1, serverListener.getMessage(0).getURN());
	assertEquals(MESSAGE_ID_2, serverListener.getMessage(1).getURN());
        assertEquals(FACTORY_1_NAME, ((TestMessage)serverListener.getMessage(0)).getDescription());
        assertEquals(FACTORY_2_NAME, ((TestMessage)serverListener.getMessage(1)).getDescription());
        // Try registering a new message factory that replaces the first one
        registry.registerMessageFactory(new TestMessageFactory(FACTORY_3_NAME, MESSAGE_ID_1));
        Message m3 = new TestMessage(MESSAGE_ID_1);
        client.sendMessage(m3);
	// Check that the third message was interpreted by the new message factory and that the old messages are still OK.
        serverListener.waitForMessages(3, TIMEOUT);
	assertEquals(3, serverListener.getMessageCount());
	assertEquals(MESSAGE_ID_1, serverListener.getMessage(0).getURN());
	assertEquals(MESSAGE_ID_2, serverListener.getMessage(1).getURN());
	assertEquals(MESSAGE_ID_1, serverListener.getMessage(2).getURN());
        assertEquals(FACTORY_1_NAME, ((TestMessage)serverListener.getMessage(0)).getDescription());
        assertEquals(FACTORY_2_NAME, ((TestMessage)serverListener.getMessage(1)).getDescription());
        assertEquals(FACTORY_3_NAME, ((TestMessage)serverListener.getMessage(2)).getDescription());
    }
}
