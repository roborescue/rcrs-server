package rescuecore2.log;

import static rescuecore2.misc.EncodingTools.writeInt32;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import rescuecore2.messages.protobuf.RCRSLogProto.LogProto;

/**
 * Abstract base class for log writer implementations.
 */
public abstract class AbstractLogWriter implements LogWriter {
	protected boolean isV2;

	public AbstractLogWriter(boolean isV2) {
		this.isV2 = isV2;
	}

	@Override
	public final void writeRecord(LogRecord entry) throws LogException {
		if (isV2)
			writeRecordProtoBuf(entry);
		else
			writeRecordV1(entry);
	}

	private final void writeRecordProtoBuf(LogRecord entry)
			throws LogException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			LogProto proto = entry.toLogProto();
//            byte[] data = JsonFormat.printer().print(proto).getBytes(); FOR JSON PRINT
			byte[] data = proto.toByteString().toByteArray();
			writeInt32(data.length, out);
			out.write(data);
			write(out.toByteArray());
		} catch (IOException e) {
			throw new LogException(e);
		}
	}

	private final void writeRecordV1(LogRecord entry) throws LogException {
		ByteArrayOutputStream gather = new ByteArrayOutputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			entry.write(gather);
			byte[] data = gather.toByteArray();
			writeInt32(entry.getRecordType().getID(), out);
			writeInt32(data.length, out);
			out.write(data);
			write(out.toByteArray());
		} catch (IOException e) {
			throw new LogException(e);
		}
	}

	/**
	 * Write a set of bytes to the log.
	 *
	 * @param bytes The bytes to write.
	 * @throws LogException If there is a problem writing the bytes.
	 */
	protected abstract void write(byte[] bytes) throws LogException;
}
