package rescuecore2.log;

import java.io.File;
import java.io.IOException;

import rescuecore2.registry.Registry;

public class RCRSLogFactory {

	public static LogReader getLogReader(String filename, Registry registry)
			throws LogException, IOException {
		if (filename.endsWith(".7z"))
			return new ZipLogReader(filename, registry);
		if (filename.endsWith(".xz"))
			return new FileLogReader(filename, registry);
		if (filename.endsWith(".log"))
			return new FileLogReaderV1(filename, registry);
		throw new LogException("Undefined Format");
	}

	public static LogWriter getLogWriter(File file)
			throws LogException, IOException {
		String filename = file.getName();
		if (filename.endsWith(".7z"))
			return new ZipLogWriter(file);
		if (filename.endsWith(".xz"))
			return new FileLogWriter(file, true);
		if (filename.endsWith(".log"))
			return new FileLogWriter(file, false);
		throw new LogException("Undefined Format");
	}
}
