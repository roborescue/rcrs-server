package rescuecore2.connection;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readBytes;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.InterruptedIOException;

import rescuecore2.misc.WorkerThread;

/**
   Connection implementation that uses InputStreams and OutputStreams.
   FIXME: Make writes asynchronous.
 */
public class StreamConnection extends AbstractConnection {
    private InputStream in;
    private OutputStream out;
    private ReadThread readThread;

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
    }

    @Override
    protected void startupImpl() {
        readThread.start();
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && readThread.isRunning();
    }

    @Override
    protected void shutdownImpl() {
        try {
            readThread.kill();
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
        //        System.out.println(this + ": sending " + b.length + " bytes");
        writeInt32(b.length, out);
        out.write(b);
        out.flush();
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
                    //                    System.err.println(StreamConnection.this + ": read thread reading " + size + " bytes");
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
                e.printStackTrace();
                return false;
            }
        }
    }
}