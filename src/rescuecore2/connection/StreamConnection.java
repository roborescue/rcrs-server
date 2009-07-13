package rescuecore2.connection;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readBytes;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.InterruptedIOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.LinkedList;

import rescuecore2.misc.WorkerThread;
import rescuecore2.misc.Pair;

/**
   Connection implementation that uses InputStreams and OutputStreams.
 */
public class StreamConnection extends AbstractConnection {
    private static final int SEND_WAIT = 10000;

    private InputStream in;
    private OutputStream out;
    private ReadThread readThread;
    private WriteThread writeThread;
    private List<byte[]> toWrite;

    /**
       Create a StreamConnection.
       @param in The InputStream to read.
       @param out The OutputStream to write to.
    */
    public StreamConnection(InputStream in, OutputStream out) {
        super();
        this.in = in;
        this.out = out;
        readThread = new ReadThread();
        writeThread = new WriteThread();
        toWrite = new LinkedList<byte[]>();
    }

    @Override
    protected void startupImpl() {
        readThread.start();
        writeThread.start();
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && readThread.isRunning() && writeThread.isRunning();
    }

    @Override
    protected void shutdownImpl() {
        System.out.println("Shutting down " + this);
        try {
            readThread.kill();
        }
        catch (InterruptedException e) {
            // Log and ignore
            // FIXME: Log it!
            e.printStackTrace();
        }
        try {
            writeThread.kill();
        }
        catch (InterruptedException e) {
            // Log and ignore
            // FIXME: Log it!
            e.printStackTrace();
        }
        try {
            out.flush();
        }
        catch (IOException e) {
            // Log and ignore
            // FIXME: Log it!
            e.printStackTrace();
        }
        try {
            out.close();
        }
        catch (IOException e) {
            // Log and ignore
            // FIXME: Log it!
            e.printStackTrace();
        }
        try {
            in.close();
        }
        catch (IOException e) {
            // Log and ignore
            // FIXME: Log it!
            e.printStackTrace();
        }
    }

    @Override
    protected void sendBytes(byte[] b) throws IOException {
        synchronized (toWrite) {
            toWrite.add(b);
            toWrite.notifyAll();
        }
    }

    /**
       Worker thread that reads from the input stream.
    */
    private class ReadThread extends WorkerThread {
        @Override
        protected boolean work() {
            try {
                //                System.out.println(StreamConnection.this + ": read thread waiting for input");
                int size = readInt32(in);
                //                System.out.println(StreamConnection.this + ": read thread reading " + size + " bytes");
                if (size > 0) {
                    //                    System.out.println(StreamConnection.this + ": read thread reading " + size + " bytes");
                    byte[] buffer = readBytes(size, in);
                    bytesReceived(buffer);
                }
                return true;
            }
            catch (InterruptedIOException e) {
                return true;
            }
            catch (EOFException e) {
                return false;
            }
            catch (IOException e) {
                System.err.println(StreamConnection.this + ": " + e);
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
       Worker thread that writes to the output stream.
    */
    private class WriteThread extends WorkerThread {
        @Override
        protected boolean work() throws InterruptedException {
            byte[] bytes = null;
            synchronized (toWrite) {
                if (toWrite.isEmpty()) {
                    toWrite.wait(SEND_WAIT);
                    return true;
                }
                else {
                    bytes = toWrite.remove(0);
                }
            }
            if (bytes == null) {
                return true;
            }
            try {
                writeInt32(bytes.length, out);
                out.write(bytes);
                out.flush();
                return true;
            }
            catch (IOException e) {
                System.err.println(StreamConnection.this + ": " + e);
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
       Create a pair of connections that pipe input to each other.
       @return A pair of connections.
    */
    public static Pair<Connection, Connection> createConnectionPair() {
        try {
            PipedInputStream in1 = new PipedInputStream();
            PipedInputStream in2 = new PipedInputStream();
            PipedOutputStream out1 = new PipedOutputStream(in2);
            PipedOutputStream out2 = new PipedOutputStream(in1);
            Connection c1 = new StreamConnection(in1, out1);
            Connection c2 = new StreamConnection(in2, out2);
            c1.startup();
            c2.startup();
            return new Pair<Connection, Connection>(c1, c2);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}