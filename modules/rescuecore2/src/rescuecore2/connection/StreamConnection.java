package rescuecore2.connection;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.InterruptedIOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.util.LinkedList;

import rescuecore2.misc.WorkerThread;
import rescuecore2.misc.EncodingTools;
import rescuecore2.misc.Pair;
import rescuecore2.registry.Registry;
import rescuecore2.log.Logger;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
 * Connection implementation that uses InputStreams and OutputStreams.
 */
public class StreamConnection extends AbstractConnection {
	private static final int SEND_WAIT = 10000;

	protected InputStream in;
	protected OutputStream out;
	private ReadThread readThread;
	private WriteThread writeThread;
	private List<MessageProto> toWrite;

	protected static final boolean GZIP_ENABLE = false;

	/**
	 * Create a StreamConnection.
	 * 
	 * @param in  The InputStream to read.
	 * @param out The OutputStream to write to.
	 * @throws IOException
	 */
	public StreamConnection(InputStream in, OutputStream out)
			throws IOException {
		super();
		if (GZIP_ENABLE) {
			LZMA2Options options = new LZMA2Options();
			options.setPreset(7); // play with this number: 6 is default but 7 works better for mid sized archives ( > 8mb)
			this.out = new XZOutputStream(out,options);
			this.out.flush();
			this.in = new XZInputStream(in);
		} else {
			this.in = in;
			this.out = out;
		}
		toWrite = new LinkedList<MessageProto>();
	}

	@Override
	protected void startupImpl() {
		Logger.debug("Starting " + this + ". Registry: "
				+ Registry.getCurrentRegistry());
		readThread = new ReadThread();
		writeThread = new WriteThread();
		readThread.start();
		writeThread.start();
	}

	@Override
	public boolean isAlive() {
		return super.isAlive() && readThread.isRunning()
				&& writeThread.isRunning();
	}

	@Override
	protected void shutdownImpl() {
		Logger.info("Shutting down " + this);
		try {
			readThread.kill();
		} catch (InterruptedException e) {
			Logger.error(
					"StreamConnection interrupted while shutting down read thread",
					e);
		}
		try {
			writeThread.kill();
		} catch (InterruptedException e) {
			Logger.error(
					"StreamConnection interrupted while shutting down write thread",
					e);
		}
		try {
			out.flush();
		} catch (IOException e) {
			Logger.error("StreamConnection error flushing output buffer", e);
		}
		try {
			out.close();
		} catch (IOException e) {
			Logger.error("StreamConnection error closing output buffer", e);
		}
		try {
			in.close();
		} catch (IOException e) {
			Logger.error("StreamConnection error closing input buffer", e);
		}
	}

//    @Override
//    protected void sendBytes(byte[] b) throws IOException {
//        synchronized (toWrite) {
//            toWrite.add(b);
//            toWrite.notifyAll();
//        }
//    }

	protected void serializeMessageProto(MessageProto messageProto)
			throws IOException {
//    	messageProto.writeDelimitedTo(out);
		byte[] bytes = messageProto.toByteArray();
		EncodingTools.writeInt32(bytes.length, out);
		out.write(bytes);

	}

	protected MessageProto deserializeMessageProto() throws IOException {
//    	return MessageProto.parseDelimitedFrom(in);
		int size = EncodingTools.readInt32(in);
		byte[] bytes = in.readNBytes(size);
		return MessageProto.parseFrom(bytes);
	}

	protected void sendMessageProto(MessageProto messageProto)
			throws IOException {
		synchronized (toWrite) {
			toWrite.add(messageProto);
			toWrite.notifyAll();
		}
	}

	/**
	 * Worker thread that reads from the input stream.
	 */
	private class ReadThread extends WorkerThread {
		@Override
		protected boolean work() {
//            byte[] buffer = {};
//	    int size = -1;
			try {
//                size = readInt32(in);
//                if (size > 0) {
//                    buffer = readBytes(size, in);
//                    bytesReceived(buffer);
//                }
//                return true;
				MessageProto messageProto = deserializeMessageProto();
				messageProtoReceived(messageProto);
				return true;
			} catch (InterruptedIOException e) {
				return true;
			} catch (EOFException e) {
				return false;
			} catch (IOException e) {
				Logger.error("Error reading from StreamConnection "
						+ StreamConnection.this, e);
				return false;
			}
		}
	}

	/**
	 * Worker thread that writes to the output stream.
	 */
	private class WriteThread extends WorkerThread {
		@Override
		protected boolean work() throws InterruptedException {
			MessageProto messageProto = null;
			synchronized (toWrite) {
				if (toWrite.isEmpty()) {
					toWrite.wait(SEND_WAIT);
					return true;
				} else {
					messageProto = toWrite.remove(0);
				}
			}
			if (messageProto == null) {
				return true;
			}
			try {
//                writeInt32(bytes.length, out);
//                out.write(bytes);
				serializeMessageProto(messageProto);
				out.flush();
				return true;
			} catch (IOException e) {
				Logger.error("Error writing to StreamConnection "
						+ StreamConnection.this, e);
				return false;
			}
		}
	}

	/**
	 * Create and start a pair of connections that pipe input to each other.
	 * 
	 * @return A pair of connections.
	 */
	public static Pair<Connection, Connection> createConnectionPair() {
		try {
			PipedInputStream in1 = new PipedInputStream();
			PipedInputStream in2 = new PipedInputStream();
			PipedOutputStream out1 = new PipedOutputStream(in2);
			PipedOutputStream out2 = new PipedOutputStream(in1);
			Connection c1 = new StreamConnection(in1, out1);
			Connection c2 = new StreamConnection(in2, out2);
			return new Pair<Connection, Connection>(c1, c2);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
