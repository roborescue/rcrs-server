package rescuecore2.log;

import java.io.OutputStream;

import rescuecore2.messages.protobuf.RCRSLogProto.LogProto;
import rescuecore2.messages.protobuf.RCRSLogProto.StartLogProto;

import java.io.InputStream;
import java.io.IOException;

/**
   Marker record at the start of the log.
*/
public class StartLogRecord implements LogRecord {
    /**
       Construct a new StartLogRecord.
     */
    public StartLogRecord() {
    }

    /**
       Construct a new StartLogRecord and read data from an InputStream.
       @param in The InputStream to read from.
       @throws IOException If there is a problem reading the stream.
     */
    public StartLogRecord(InputStream in) throws IOException {
        read(in);
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.START_OF_LOG;
    }

    @Override
    public void write(OutputStream out) throws IOException {
    }

    @Override
    public void read(InputStream in) throws IOException {
    }

	@Override
	public void fromLogProto(LogProto log) {
		
	}

	@Override
	public LogProto toLogProto() {
		return LogProto.newBuilder().setStart(StartLogProto.newBuilder()).build();
	}
}
