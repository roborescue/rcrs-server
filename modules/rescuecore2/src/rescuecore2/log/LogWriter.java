package rescuecore2.log;

/**
   An interface for objects that know how to write log entries.
 */
public interface LogWriter {
    /**
       Write a log entry.
       @param entry The entry to write.
       @throws LogException If there is a problem writing the log.
    */
    void writeRecord(LogRecord entry) throws LogException;

    /**
       Close the log.
    */
    void close();
}
