package rescuecore2.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import rescuecore2.messages.Message;
import rescuecore2.messages.MessageFactory;
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
    protected MessageFactory factory;

    private static final int TIMEOUT = 3000;

    @Before
    public void setup() throws IOException {
	factory = new TestMessageFactory();
	Pair<Connection, Connection> connections = makeConnectionPair(factory);
	client = connections.first();
	server = connections.second();
	clientListener = new TestConnectionListener();
	serverListener = new TestConnectionListener();
	client.addConnectionListener(clientListener);
	server.addConnectionListener(serverListener);
    }

    /**
       Get two connection objects that represent the two end-points of a communication channel.
    */
    protected abstract Pair<Connection, Connection> makeConnectionPair(MessageFactory factory) throws IOException;

    @Test
    public void testNoMessagesReceivedBeforeStartup() throws IOException, InterruptedException, ConnectionException {
	// Send a message from the client
	client.startup();
	Message m = factory.createMessage(TestMessageFactory.MESSAGE_1);
	client.sendMessage(m);
	// Wait a bit
	Thread.sleep(TIMEOUT);
	assertEquals(0, serverListener.getMessageCount());
	// Start the server connection
	server.startup();
	Thread.sleep(TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
    }

    @Test
    public void testNoMessagesAfterShutdown() throws IOException, InterruptedException, ConnectionException {
	// Send a message from the client
	client.startup();
	server.startup();
	Message m = factory.createMessage(TestMessageFactory.MESSAGE_1);
	client.sendMessage(m);
	// Wait a bit
	Thread.sleep(TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
	// Shutdown the server connection
	server.shutdown();
	// Send another message and check that it didn't arrive
	try {
	    client.sendMessage(m);
	}
	catch (IOException e) {
	    // Could reasonably expect an IOException right now: the server is gone.
	}
	Thread.sleep(TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
    }

    @Test
    public void testRemoveConnectionListener() throws IOException, InterruptedException, ConnectionException {
	client.startup();
	server.startup();
	// Send a message from the client
	Message m = factory.createMessage(TestMessageFactory.MESSAGE_1);
	client.sendMessage(m);
	// Wait a bit
	Thread.sleep(TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
	// Remove the listener
	server.removeConnectionListener(serverListener);
	// Send another message
	client.sendMessage(m);
	// Wait a bit
	Thread.sleep(TIMEOUT);
	// Check that the new message didn't arrive
	assertEquals(1, serverListener.getMessageCount());
    }

    @Test
    public void testMultipleStartup() throws IOException, InterruptedException, ConnectionException {
	client.startup();
	client.startup();
	server.startup();
	// Send a message from the client
	Message m = factory.createMessage(TestMessageFactory.MESSAGE_1);
	client.sendMessage(m);
	// Wait a bit
	Thread.sleep(TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
	client.startup();
	client.sendMessage(m);
	// Wait a bit
	Thread.sleep(TIMEOUT);
	assertEquals(2, serverListener.getMessageCount());
    }

    @Test
    public void testMultipleShutdown() throws IOException, InterruptedException, ConnectionException {
	client.startup();
	server.startup();
	// Send a message from the client
	Message m = factory.createMessage(TestMessageFactory.MESSAGE_1);
	client.sendMessage(m);
	// Wait a bit
	Thread.sleep(TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
	server.shutdown();
	// Send another message
	try {
	    client.sendMessage(m);
	}
	catch (IOException e) {
	    // Could reasonably expect an IOException right now: the server is gone.
	}
	// Wait a bit
	Thread.sleep(TIMEOUT);
	// Check that the new message didn't arrive
	assertEquals(1, serverListener.getMessageCount());
	server.shutdown();
	// Send another message
	try {
	    client.sendMessage(m);
	}
	catch (IOException e) {
	    // Could reasonably expect an IOException right now: the server is gone.
	}
	// Wait a bit
	Thread.sleep(TIMEOUT);
	// Check that the new message didn't arrive
	assertEquals(1, serverListener.getMessageCount());
    }

    @Test
    public void testSendMessage() throws IOException, InterruptedException, ConnectionException {
	client.startup();
	server.startup();
	Message m = factory.createMessage(TestMessageFactory.MESSAGE_1);
	client.sendMessage(m);
	Thread.sleep(TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
	assertEquals(m, serverListener.getMessage(0));
    }

    @Test
    public void testSendMessages() throws IOException, InterruptedException, ConnectionException{
	client.startup();
	server.startup();
	Message m1 = new TestMessage(TestMessageFactory.MESSAGE_1, 3);
	Message m2 = new TestMessage(TestMessageFactory.MESSAGE_1, 4, 5);
	client.sendMessages(Arrays.asList(m1, m2));
	Thread.sleep(TIMEOUT);
	assertEquals(2, serverListener.getMessageCount());
	assertEquals(m1, serverListener.getMessage(0));
	assertEquals(m2, serverListener.getMessage(1));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSendNullMessage() throws IOException, InterruptedException, ConnectionException {
	client.startup();
	server.startup();
	client.sendMessage(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSendNullMessages() throws IOException, InterruptedException, ConnectionException {
	client.startup();
	server.startup();
	client.sendMessages(null);
    }

    @Test(expected=rescuecore2.connection.ConnectionException.class)
    public void testSendMessageBeforeStartup() throws IOException, InterruptedException, ConnectionException {
	server.startup();
	Message m = factory.createMessage(TestMessageFactory.MESSAGE_1);
	client.sendMessage(m);
    }

    @Test(expected=rescuecore2.connection.ConnectionException.class)
    public void testSendMessageAfterShutdown() throws IOException, InterruptedException, ConnectionException {
	server.startup();
	Message m = factory.createMessage(TestMessageFactory.MESSAGE_1);
	client.sendMessage(m);
	Thread.sleep(TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
	client.shutdown();
	client.sendMessage(m);
    }

    @Test
    public void testSetMessageFactory() throws IOException, InterruptedException, ConnectionException {
	client.startup();
	server.startup();
	Message m = factory.createMessage(TestMessageFactory.MESSAGE_1);
	client.sendMessage(m);
	Thread.sleep(TIMEOUT);
	assertEquals(1, serverListener.getMessageCount());
	assertEquals(m, serverListener.getMessage(0));
	server.setMessageFactory(new NewMessageFactory());
	client.sendMessage(m);
	// Check that the second message was interpreted by the new message factory and that the old message is still OK.
	Thread.sleep(TIMEOUT);
	assertEquals(2, serverListener.getMessageCount());
	assertEquals(TestMessageFactory.MESSAGE_1, serverListener.getMessage(0).getMessageTypeID());
	assertEquals(TestMessageFactory.MESSAGE_2, serverListener.getMessage(1).getMessageTypeID());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetNullMessageFactory() throws IOException, InterruptedException {
	client.setMessageFactory(null);
    }

    private class NewMessageFactory implements MessageFactory {
	public Message createMessage(int id) {
	    // Switch the IDs
	    switch (id) {
	    case TestMessageFactory.MESSAGE_1:
		return new TestMessage(TestMessageFactory.MESSAGE_2);
	    case TestMessageFactory.MESSAGE_2:
		return new TestMessage(TestMessageFactory.MESSAGE_1);
	    }
	    throw new IllegalArgumentException("Unrecognised ID: " + id);
	}
    }
}