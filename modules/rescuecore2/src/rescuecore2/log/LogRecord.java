package rescuecore2.log;

import java.io.OutputStream;

import rescuecore2.messages.protobuf.RCRSLogProto.LogProto;

import java.io.InputStream;
import java.io.IOException;

/**
   Interface for entries in the log.
*/
public interface LogRecord {
    /**
       Get the type of this record.
       @return The record type.
    */
    RecordType getRecordType();

    /**
       Write this log record to a stream.
       @param out The OutputStream to write to.
       @throws IOException If there is a problem writing to the stream.
    */
    void write(OutputStream out) throws IOException;

    /**
       Read this log record's data from a stream.
       @param in The InputStream to read from.
       @throws IOException If there is a problem reading the stream.
       @throws LogException If there is a problem reading the log record.
    */
    void read(InputStream in) throws IOException, LogException;
    
    void fromLogProto(LogProto log) throws LogException;
    LogProto toLogProto();
}
