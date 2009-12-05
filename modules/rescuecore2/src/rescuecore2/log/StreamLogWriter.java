package rescuecore2.log;

import java.io.OutputStream;
import java.io.IOException;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A class for writing the kernel log to an output stream.
 */
public class StreamLogWriter extends AbstractLogWriter {
    private static final Log LOG = LogFactory.getLog(StreamLogWriter.class);

    private OutputStream out;

    /**
       Create a stream log writer.
       @param stream The stream to write to.
    */
    public StreamLogWriter(OutputStream stream) {
        this.out = stream;
    }

    @Override
    protected void write(byte[] bytes) throws LogException {
        try {
            out.write(bytes);
        }
        catch (IOException e) {
            throw new LogException(e);
        }
    }

    @Override
    public void close() {
        try {
            out.flush();
        }
        catch (IOException e) {
            LOG.error("Error flushing log stream", e);
        }
        try {
            out.close();
        }
        catch (IOException e) {
            LOG.error("Error closing log stream", e);
        }
    }
}