package rescuecore2.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.io.IOException;
import java.net.Socket;

import rescuecore2.messages.MessageRegistry;

public class ConnectionManagerTest {
    private static final int PORT = 34332;
    private static final int PORT2 = 34333;
    private static final int DELAY = 1000;
    private static final int LONG_DELAY = 5000;
    private static final int MESSAGE_1 = 0x0100;

    private ConnectionManager manager;
    private TestConnectionManagerListener listener;

    @Before
    public void setup() {
        manager = new ConnectionManager();
        listener = new TestConnectionManagerListener();
        MessageRegistry.register(new TestMessageFactory("", MESSAGE_1));
    }

    @After
    public void cleanup() {
        manager.shutdown();
    }

    @Test
    public void testListen() throws IOException, InterruptedException {
        manager.listen(PORT, listener);
        // Check that connecting to the socket results in a new Connection.
        new Socket("localhost", PORT);
        Thread.sleep(DELAY);
        assertEquals(1, listener.getCount());
        // Check that sleeping for a bit allows new connections in the future
        Thread.sleep(LONG_DELAY);
        new Socket("localhost", PORT);
        Thread.sleep(DELAY);
        assertEquals(2, listener.getCount());
    }

    @Test
    public void testListenMultiplePorts() throws IOException, InterruptedException {
        manager.listen(PORT, listener);
        manager.listen(PORT2, listener);
        // Check that connecting to each socket results in a new Connection.
        new Socket("localhost", PORT);
        new Socket("localhost", PORT2);
        Thread.sleep(DELAY);
        assertEquals(2, listener.getCount());
    }

    @Test
    public void testShutdown() throws IOException, InterruptedException {
        manager.listen(PORT, listener);
        manager.listen(PORT2, listener);
        // Check that connecting to each socket results in a new Connection.
        new Socket("localhost", PORT);
        new Socket("localhost", PORT2);
        Thread.sleep(DELAY);
        assertEquals(2, listener.getCount());
        assertTrue(manager.isAlive());
        manager.shutdown();
        assertFalse(manager.isAlive());
        // Check that further connections are rejected
        try {
            new Socket("localhost", PORT);
            fail("Expected an IOException");
        }
        catch (IOException e) {
            // Expected
        }
        try {
            new Socket("localhost", PORT2);
            fail("Expected an IOException");
        }
        catch (IOException e) {
            // Expected
        }
        Thread.sleep(DELAY);
        assertEquals(2, listener.getCount());
    }

    @Test
    public void testShutdownPreventsListen() throws IOException, InterruptedException {
        manager.listen(PORT, listener);
        // Check that connecting to the socket results in a new Connection.
        new Socket("localhost", PORT);
        Thread.sleep(DELAY);
        assertEquals(1, listener.getCount());
        manager.shutdown();
        // Check that attempting to listen again fails
        try {
            manager.listen(PORT2, listener);
            fail("Expected an IOException when trying to listen after shutdown");
        }
        catch (IOException e) {
            // Expected
        }
        try {
            new Socket("localhost", PORT2);
            fail("Expected an IOException");
        }
        catch (IOException e) {
            // Expected
        }
        Thread.sleep(DELAY);
        assertEquals(1, listener.getCount());
    }

    @Test
    public void testInterruptedShutdown() throws IOException, InterruptedException {
        manager.listen(PORT, listener);
        // Check that connecting to the socket results in a new Connection.
        new Socket("localhost", PORT);
        Thread.sleep(DELAY);
        assertEquals(1, listener.getCount());
        Thread.currentThread().interrupt();
        manager.shutdown();
        // Check that connecting to the port fails
        // This may or may not throw an exception, but the new connection should not be registered even if it arrives before the server socket closes.
        try {
            new Socket("localhost", PORT);
        }
        catch (IOException e) {
            // Expected
        }
        assertEquals(1, listener.getCount());
        // Check that attempting to listen again fails
        try {
            manager.listen(PORT2, listener);
            fail("Expected an IOException when trying to listen after shutdown");
        }
        catch (IOException e) {
            // Expected
        }
        try {
            new Socket("localhost", PORT2);
            fail("Expected an IOException");
        }
        catch (IOException e) {
            // Expected
        }
        Thread.sleep(DELAY);
        assertEquals(1, listener.getCount());
    }

    private class TestConnectionManagerListener implements ConnectionManagerListener {
        private int count;

        public TestConnectionManagerListener() {
            count = 0;
        }

        @Override
        public synchronized void newConnection(Connection c) {
            ++count;
        }

        public synchronized int getCount() {
            return count;
        }
    }
}