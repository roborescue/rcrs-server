package rescuecore2.log;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readInt32;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.protobuf.RCRSLogProto.LogProto;
import rescuecore2.messages.protobuf.RCRSLogProto.UpdatesLogProto;
import rescuecore2.worldmodel.ChangeSet;

/**
 * An updates record.
 */
public class UpdatesRecord implements LogRecord {
	private int time;
	private ChangeSet changes;

	/**
	 * Construct a new UpdatesRecord.
	 * 
	 * @param time    The timestep of this updates record.
	 * @param changes The set of changes.
	 */
	public UpdatesRecord(int time, ChangeSet changes) {
		this.time = time;
		this.changes = changes;
	}

	/**
	 * Construct a new UpdatesRecord and read data from an InputStream.
	 * 
	 * @param in The InputStream to read from.
	 * @throws IOException  If there is a problem reading the stream.
	 * @throws LogException If there is a problem reading the log record.
	 */
	public UpdatesRecord(InputStream in) throws IOException, LogException {
		read(in);
	}

	public UpdatesRecord(LogProto log) {
		fromLogProto(log);
	}

	@Override
	public RecordType getRecordType() {
		return RecordType.UPDATES;
	}

	@Override
	public void write(OutputStream out) throws IOException {
		writeInt32(time, out);
		changes.write(out);
	}

	@Override
	public void read(InputStream in) throws IOException, LogException {
		time = readInt32(in);
		changes = new ChangeSet();
		changes.read(in);
	}

	/**
	 * Get the timestamp for this record.
	 * 
	 * @return The timestamp.
	 */
	public int getTime() {
		return time;
	}

	/**
	 * Get the entity updates.
	 * 
	 * @return The changes.
	 */
	public ChangeSet getChangeSet() {
		return changes;
	}

	@Override
	public void fromLogProto(LogProto log) {
		time = log.getUpdate().getTime();
		changes = new ChangeSet();
		changes.fromChangeSetProto(log.getUpdate().getChanges());
	}

	@Override
	public LogProto toLogProto() {
		return LogProto
				.newBuilder().setUpdate(UpdatesLogProto.newBuilder()
						.setTime(time).setChanges(changes.toChangeSetProto()))
				.build();
	}
}
