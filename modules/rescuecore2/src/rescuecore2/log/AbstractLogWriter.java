package rescuecore2.log;

import static rescuecore2.misc.EncodingTools.writeInt32;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
   Abstract base class for log writer implementations.
 */
public abstract class AbstractLogWriter implements LogWriter {
    @Override
    public final void writeRecord(LogRecord entry) throws LogException {
        ByteArrayOutputStream gather = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            entry.write(gather);
            byte[] data = gather.toByteArray();
            writeInt32(entry.getRecordType().getID(), out);
            writeInt32(data.length, out);
            out.write(data);
            write(out.toByteArray());
        }
        catch (IOException e) {
            throw new LogException(e);
        }
    }

    /**
       Write a set of bytes to the log.
       @param bytes The bytes to write.
       @throws LogException If there is a problem writing the bytes.
     */
    protected abstract void write(byte[] bytes) throws LogException;
}
