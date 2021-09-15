package rescuecore2.log;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.writeString;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readString;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import rescuecore2.config.Config;
import rescuecore2.messages.protobuf.RCRSLogProto.ConfigLogProto;
import rescuecore2.messages.protobuf.RCRSLogProto.LogProto;
import rescuecore2.messages.protobuf.RCRSProto.ConfigProto;

/**
 * A configuration record.
 */
public class ConfigRecord implements LogRecord {
	private Config config;

	/**
	 * Construct a new ConfigRecord.
	 * 
	 * @param config The Config to record.
	 */
	public ConfigRecord(Config config) {
		this.config = config;
	}

	/**
	 * Construct a new ConfigRecord and read data from an InputStream.
	 * 
	 * @param in The InputStream to read from.
	 * @throws IOException If there is a problem reading the stream.
	 */
	public ConfigRecord(InputStream in) throws IOException {
		read(in);
	}

	public ConfigRecord(LogProto log) {
		fromLogProto(log);
	}

	@Override
	public RecordType getRecordType() {
		return RecordType.CONFIG;
	}

	@Override
	public void write(OutputStream out) throws IOException {
		Set<String> all = config.getAllKeys();
		writeInt32(all.size(), out);
		for (String key : all) {
			String value = config.getValue(key);
			writeString(key, out);
			writeString(value, out);
		}
	}

	@Override
	public void read(InputStream in) throws IOException {
		config = new Config();
		int size = readInt32(in);
		for (int i = 0; i < size; ++i) {
			String key = readString(in);
			String value = readString(in);
			config.setValue(key, value);
		}
	}

	/**
	 * Get the Config.
	 * 
	 * @return The config.
	 */
	public Config getConfig() {
		return config;
	}

	@Override
	public void fromLogProto(LogProto log) {
		config = new Config();
		for (Entry<String, String> entry : log.getConfig().getConfig()
				.getDataMap().entrySet()) {
			config.setValue(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public LogProto toLogProto() {
		return LogProto.newBuilder()
				.setConfig(ConfigLogProto.newBuilder().setConfig(
						ConfigProto.newBuilder().putAllData(config.getData())))
				.build();
	}
}
