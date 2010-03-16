package rescuecore2.connection;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import rescuecore2.messages.Message;
import rescuecore2.misc.Pair;

public class StreamConnectionTest extends ConnectionTestCommon {
    private static final byte[] TEST_DATA = {0x01, 0x02, 0x03, 0x04};
    private static final byte[] EXPECTED_TEST_OUTPUT = {0x00, 0x00, 0x00, 0x04,
                                                        0x01, 0x02, 0x03, 0x04};
    private static final byte[] GOOD_INPUT = {0x00, 0x00, 0x00, 0x08,
                                              0x01, 0x02, 0x03, 0x04,
                                              0x00, 0x00, 0x00, 0x00};
    private static final byte[] NEGATIVE_SIZE_INPUT = {(byte)0x80, 0x00, 0x00, 0x00, // Malformed size
                                                       0x00, 0x00, 0x00, 0x10, // Good size
                                                       0x00, 0x00, 0x00, 0x04, // Size of URN
                                                       0x54, 0x65, 0x73, 0x74, // URN ('Test')
                                                       0x00, 0x00, 0x00, 0x04, // Size of message
                                                       0x00, 0x00, 0x00, 0x00  // Message data
    };
    
    // One byte short
    private static final byte[] SHORT_CONTENT = {0x00, 0x00, 0x00, 0x04,
                                                 0x01, 0x02, 0x03};
    private static final byte[] SHORT_SIZE_FIELD = {0x00, 0x00, 0x01};
    private static final String MESSAGE_URN = "Test";

    @Override
    protected Pair<Connection, Connection> makeConnectionPair() throws IOException {
        PipedInputStream serverIn = new PipedInputStream();
        PipedInputStream clientIn = new PipedInputStream();
        PipedOutputStream serverOut = new PipedOutputStream(clientIn);
        PipedOutputStream clientOut = new PipedOutputStream(serverIn);
	Connection client = new StreamConnection(clientIn, clientOut);
	Connection server = new StreamConnection(serverIn, serverOut);
	return new Pair<Connection, Connection>(client, server);
    }

    @Test
    public void testSendBytes() throws IOException, InterruptedException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        TestInputStream in = new TestInputStream();
        TestOutputStream out = new TestOutputStream(bout);
        StreamConnection c = new StreamConnection(in, out);
        c.startup();
        c.sendBytes(TEST_DATA);
        // Wait for a bit
        Thread.sleep(DELAY);
        assertArrayEquals(EXPECTED_TEST_OUTPUT, bout.toByteArray());
    }

    @Test
    public void testIOExceptionOnReadSize() throws IOException, InterruptedException {
        TestInputStream in = new TestInputStream(GOOD_INPUT);
        TestOutputStream out = new TestOutputStream();
        Connection c = new StreamConnection(in, out);
        TestConnectionListener l = new TestConnectionListener();
        c.addConnectionListener(l);
        in.setFailOnRead(true);
        // Should fail immediately
        c.startup();
	Thread.sleep(DELAY);
        assertFalse(c.isAlive());
        assertEquals(0, l.getMessageCount());
    }

    @Test
    public void testIOExceptionOnReadSize2() throws IOException, InterruptedException {
        TestInputStream in = new TestInputStream(GOOD_INPUT);
        TestOutputStream out = new TestOutputStream();
        Connection c = new StreamConnection(in, out);
        TestConnectionListener l = new TestConnectionListener();
        c.addConnectionListener(l);
        in.setFailOnRead(2);
        // Should fail after reading two size bytes
        c.startup();
	Thread.sleep(DELAY);
        assertFalse(c.isAlive());
        assertEquals(0, l.getMessageCount());
    }

    @Test
    public void testIOExceptionOnReadContent() throws IOException, InterruptedException {
        TestInputStream in = new TestInputStream(GOOD_INPUT);
        TestOutputStream out = new TestOutputStream();
        Connection c = new StreamConnection(in, out);
        TestConnectionListener l = new TestConnectionListener();
        c.addConnectionListener(l);
        in.setFailOnRead(6);
        // Should fail after reading the size header and 2 bytes of input
        c.startup();
	Thread.sleep(DELAY);
        assertFalse(c.isAlive());
        assertEquals(0, l.getMessageCount());
    }

    @Test
    public void testNegativeSizeInput() throws IOException, InterruptedException {
        System.err.println("Test negative size input");
        try {
            registry.registerMessageFactory(new TestMessageFactory("StreamConnectionTest factory", MESSAGE_URN));
            TestInputStream in = new TestInputStream(NEGATIVE_SIZE_INPUT);
            TestOutputStream out = new TestOutputStream();
            Connection c = new StreamConnection(in, out);
            TestConnectionListener l = new TestConnectionListener();
            c.setRegistry(registry);
            c.addConnectionListener(l);
            c.startup();
            // Should ignore the first negative size field then read a message with urn 'T'
            l.waitForMessages(1, TIMEOUT);
            assertEquals(1, l.getMessageCount());
            assertEquals(MESSAGE_URN, l.getMessage(0).getURN());
        }
        finally {
            System.err.println("Test negative size input finished");
        }
    }

    @Test
    public void testShortContent() throws IOException, InterruptedException {
        TestInputStream in = new TestInputStream(SHORT_CONTENT);
        TestOutputStream out = new TestOutputStream();
        Connection c = new StreamConnection(in, out);
        TestConnectionListener l = new TestConnectionListener();
        c.addConnectionListener(l);
        c.startup();
        // Should not read any messages at all
        l.waitForMessages(1, TIMEOUT);
        assertEquals(0, l.getMessageCount());
        assertFalse(c.isAlive());
    }

    @Test
    public void testShortSizeField() throws IOException, InterruptedException {
        TestInputStream in = new TestInputStream(SHORT_SIZE_FIELD);
        TestOutputStream out = new TestOutputStream();
        Connection c = new StreamConnection(in, out);
        TestConnectionListener l = new TestConnectionListener();
        c.addConnectionListener(l);
        c.startup();
        // Should not read any messages at all
        l.waitForMessages(1, TIMEOUT);
        assertEquals(0, l.getMessageCount());
        assertFalse(c.isAlive());
    }

    @Test
    public void testExceptionOnOutputFlush() throws IOException {
        TestInputStream in = new TestInputStream();
        TestOutputStream out = new TestOutputStream();
        Connection c = new StreamConnection(in, out);
        out.setFailOnFlush(true);
        c.startup();
        assertTrue(c.isAlive());
        c.shutdown();
        assertFalse(c.isAlive());
    }

    @Test
    public void testExceptionOnOutputClose() throws IOException {
        TestInputStream in = new TestInputStream();
        TestOutputStream out = new TestOutputStream();
        Connection c = new StreamConnection(in, out);
        out.setFailOnClose(true);
        c.startup();
        assertTrue(c.isAlive());
        c.shutdown();
        assertFalse(c.isAlive());
    }

    @Test
    public void testExceptionOnInputClose() throws IOException {
        TestInputStream in = new TestInputStream();
        TestOutputStream out = new TestOutputStream();
        Connection c = new StreamConnection(in, out);
        in.setFailOnClose(true);
        c.startup();
        assertTrue(c.isAlive());
        c.shutdown();
        assertFalse(c.isAlive());
    }

    @Test
    public void testInterruptedDuringShutdown() throws IOException {
        TestInputStream in = new TestInputStream();
        TestOutputStream out = new TestOutputStream();
        Connection c = new StreamConnection(in, out);
        c.startup();
        assertTrue(c.isAlive());
        Thread.currentThread().interrupt();
        c.shutdown();
        assertFalse(c.isAlive());
    }

    private class TestInputStream extends InputStream {
        private InputStream upstream;
        private boolean failOnRead;
        private boolean failOnClose;
        private int failCount;

        public TestInputStream() {
            this((InputStream)null);
        }

        public TestInputStream(byte[] bytes) {
            this(new ByteArrayInputStream(bytes));
        }

        public TestInputStream(InputStream upstream) {
            this.upstream = upstream;
            failOnRead = false;
            failOnClose = false;
            failCount = -1;
        }

        @Override
        public int read() throws IOException {
            if (--failCount == 0) {
                failOnRead = true;
            }
            if (failOnRead) {
                throw new IOException("Fail on read");
            }
            if (upstream != null) {
                return upstream.read();
            }
            else {
                // Block
                while (true) {
                    try {
                        Thread.sleep(DELAY);
                    }
                    catch (InterruptedException e) {
                        throw new InterruptedIOException();
                    }
                }
            }
        }

        @Override
        public void close() throws IOException {
            if (failOnClose) {
                throw new IOException("Fail on close");
            }
            if (upstream != null) {
                upstream.close();
            }
        }

        public void setFailOnRead(boolean b) {
            failOnRead = b;
        }

        public void setFailOnRead(int count) {
            failCount = count;
        }

        public void setFailOnClose(boolean b) {
            failOnClose = b;
        }
    }

    private class TestOutputStream extends OutputStream {
        private OutputStream downstream;
        private boolean failOnWrite;
        private boolean failOnFlush;
        private boolean failOnClose;

        public TestOutputStream() {
            this(null);
        }

        public TestOutputStream(OutputStream downstream) {
            this.downstream = downstream;
            failOnWrite = false;
            failOnFlush = false;
            failOnClose = false;
        }

        @Override
        public void write(int i) throws IOException {
            if (failOnWrite) {
                throw new IOException("Fail on write");
            }
            if (downstream != null) {
                downstream.write(i);
            }
        }

        @Override
        public void flush() throws IOException {
            if (failOnFlush) {
                throw new IOException("Fail on flush");
            }
            if (downstream != null) {
                downstream.flush();
            }
        }

        @Override
        public void close() throws IOException {
            if (failOnClose) {
                throw new IOException("Fail on close");
            }
            if (downstream != null) {
                downstream.close();
            }
        }

        public void setFailOnWrite(boolean b) {
            failOnWrite = b;
        }

        public void setFailOnFlush(boolean b) {
            failOnFlush = b;
        }

        public void setFailOnClose(boolean b) {
            failOnClose = b;
        }
    }
}
