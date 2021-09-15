package rescuecore2.log;

import java.io.OutputStream;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.IOException;

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
	public StreamLogWriter(OutputStream stream) throws IOException {
		LZMA2Options options = new LZMA2Options();
		options.setPreset(7); // play with this number: 6 is default but 7 works better for mid sized archives ( > 8mb)
		this.out = new XZOutputStream(stream,options);
		
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
		try {
			if(out instanceof XZOutputStream)
				((XZOutputStream)out).finish();
		} catch (IOException e) {
			Logger.error("Error finishing XZ stream", e);
		}
		try {
			out.flush();
		} catch (IOException e) {
			Logger.error("Error flushing log stream", e);
		}
		try {
			out.close();
		} catch (IOException e) {
			Logger.error("Error closing log stream", e);
		}
	}
}
