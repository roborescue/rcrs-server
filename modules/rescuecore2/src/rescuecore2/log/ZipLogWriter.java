package rescuecore2.log;

import java.io.File;
import java.io.IOException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

public class ZipLogWriter implements LogWriter {

	private SevenZOutputFile sevenZOutput;

	public ZipLogWriter(File file) throws IOException {
		sevenZOutput = new SevenZOutputFile(file);
	}

	@Override
	public void writeRecord(LogRecord record) throws LogException {
		try {
			sevenZOutput.putArchiveEntry(getRecordArchive(record));
			sevenZOutput.write(record.toLogProto().toByteArray());
			sevenZOutput.closeArchiveEntry();
		} catch (IOException e) {
			throw new LogException(e);
		}
	}

	@Override
	public void close() {
		try {
			sevenZOutput.close();
		} catch (IOException e) {
			Logger.error("Error closing log stream", e);
		}
	}

	private SevenZArchiveEntry createArchiveEntry(String path) {
		final SevenZArchiveEntry entry = new SevenZArchiveEntry();
		entry.setDirectory(false);
		entry.setName(path);
		return entry;
	}

	private ArchiveEntry getRecordArchive(LogRecord record) {
		String recordType = record.getRecordType().name();
		switch (record.getRecordType()) {
		case COMMANDS:
			CommandsRecord crecord = (CommandsRecord) record;
			return createArchiveEntry(crecord.getTime() + "/" + recordType);
		case PERCEPTION:
			PerceptionRecord precord = (PerceptionRecord) record;
			return createArchiveEntry(precord.getTime() + "/" + recordType + "/"
					+ precord.getEntityID());
		case UPDATES:
			UpdatesRecord urecord = (UpdatesRecord) record;
			return createArchiveEntry(urecord.getTime() + "/" + recordType);
		case CONFIG:
		case INITIAL_CONDITIONS:
		case START_OF_LOG:
		case END_OF_LOG:
			return createArchiveEntry(recordType);

		default:
			throw new IllegalArgumentException(
					"Unexpected value: " + record.getRecordType());
		}

	}

}
