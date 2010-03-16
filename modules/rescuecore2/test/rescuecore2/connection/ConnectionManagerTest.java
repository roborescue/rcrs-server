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

import rescuecore2.registry.Registry;

public class ConnectionManagerTest {
    private static final int PORT = 34332;
    private static final int PORT2 = 34333;
    private static final int DELAY = 1000;
    private static final int TIMEOUT = 1000;
    private static final String MESSAGE_1 = "Message 1";

    private ConnectionManager manager;
    private TestConnectionManagerListener listener;
    private Registry registry;

    @Before
    public void setup() {
        manager = new ConnectionManager();
        listener = new TestConnectionManagerListener();
        registry = new Registry();
        registry.registerMessageFactory(new TestMessageFactory("", MESSAGE_1));
    }

    @After
    public void cleanup() {
        manager.shutdown();
    }

    @Test
    public void testListen() throws IOException, InterruptedException {
        manager.listen(PORT, registry, listener);
        // Check that connecting to the socket results in a new Connection.
        new Socket("localhost", PORT);
        assertTrue(listener.waitForCount(1, TIMEOUT));
        // Sleep for a bit and make a new connection
        Thread.sleep(DELAY);
        new Socket("localhost", PORT);
        assertTrue(listener.waitForCount(2, TIMEOUT));
        // Make a bunch of new connections and check that they all arrive
        new Socket("localhost", PORT);
        new Socket("localhost", PORT);
        new Socket("localhost", PORT);
        new Socket("localhost", PORT);
        assertTrue(listener.waitForCount(6, TIMEOUT));
    }

    @Test
    public void testListenMultiplePorts() throws IOException, InterruptedException {
        manager.listen(PORT, registry, listener);
        manager.listen(PORT2, registry, listener);
        // Check that connecting to each socket results in a new Connection.
        new Socket("localhost", PORT);
        new Socket("localhost", PORT2);
        assertTrue(listener.waitForCount(2, TIMEOUT));
    }

    @Test
    public void testShutdown() throws IOException, InterruptedException {
        manager.listen(PORT, registry, listener);
        manager.listen(PORT2, registry, listener);
        // Check that connecting to each socket results in a new Connection.
        new Socket("localhost", PORT);
        new Socket("localhost", PORT2);
        assertTrue(listener.waitForCount(2, TIMEOUT));
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
        manager.listen(PORT, registry, listener);
        // Check that connecting to the socket results in a new Connection.
        new Socket("localhost", PORT);
        assertTrue(listener.waitForCount(1, TIMEOUT));
        manager.shutdown();
        // Check that attempting to listen again fails
        try {
            manager.listen(PORT2, registry, listener);
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
        manager.listen(PORT, registry, listener);
        // Check that connecting to the socket results in a new Connection.
        new Socket("localhost", PORT);
        assertTrue(listener.waitForCount(1, TIMEOUT));
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
        Thread.sleep(DELAY);
        assertEquals(1, listener.getCount());
        // Check that attempting to listen again fails
        try {
            manager.listen(PORT2, registry, listener);
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
            this.notifyAll();
        }

        public synchronized int getCount() {
            return count;
        }

        public synchronized boolean waitForCount(int goal, long timeout) {
            long end = System.currentTimeMillis() + timeout;
            while (count < goal) {
                long now = System.currentTimeMillis();
                if (now > end) {
                    return false;
                }
                try {
                    wait(end - now);
                }
                catch (InterruptedException e) {
                    return count >= goal;
                }
            }
            return true;
        }
    }
}
