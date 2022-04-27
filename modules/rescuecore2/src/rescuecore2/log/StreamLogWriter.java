package rescuecore2.log;

import java.io.IOException;
import java.io.OutputStream;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAOutputStream;

/**
 * A class for writing the kernel log to an output stream.
 */
public class StreamLogWriter extends AbstractLogWriter {
	private OutputStream out;

	/**
	 * Create a stream log writer.
	 *
	 * @param stream The stream to write to.
	 * @throws IOException
	 */
	public StreamLogWriter(OutputStream stream, boolean isV2)
			throws IOException {
		super(isV2);
		if (isV2)
			this.out = new LZMAOutputStream(stream, new LZMA2Options(7), -1);
	}

	@Override
	protected void write(byte[] bytes) throws LogException {
		try {
			out.write(bytes);
		} catch (IOException e) {
			throw new LogException(e);
		}
	}

	@Override
	public void close() {
		if (!isV2) {
			/* not supported for LZMA */
			try {
				out.flush();
			} catch (IOException e) {
				Logger.error("Error flushing log stream", e);
			}
		}
		try {
			out.close();
		} catch (IOException e) {
			Logger.error("Error closing log stream", e);
		}
	}

}
