package rescuecore2.log;

import static rescuecore2.misc.EncodingTools.readBytes;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.reallySkip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.RandomAccessFile;
import java.io.ByteArrayInputStream;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.TreeMap;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.config.Config;
import rescuecore2.registry.Registry;

/**
   A log reader that reads from a file.
 */
public class FileLogReader extends StreamLogReader {
    public FileLogReader(String name, Registry registry) throws IOException, LogException {
        this(new File(name), registry);
    }

    /**
       Construct a new FileLogReader.
       @param file The file object to read.
       @param registry The registry to use for reading log entries.
       @throws IOException If the file cannot be read.
       @throws LogException If there is a problem reading the log.
    */
    public FileLogReader(File file, Registry registry) throws IOException, LogException {
        super(new FileInputStream(file),registry);
        Logger.info("Reading file log: " + file.getAbsolutePath());
    }
}
