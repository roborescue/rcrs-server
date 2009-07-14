package kernel.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
   A log reader that reads from a file.
 */
public class FileLogReader extends StreamLogReader {
    /**
       Construct a new FileLogReader.
       @param name The name of the file to read.
       @throws IOException If the file cannot be read.
       @throws KernelLogException If there is a problem reading the log.
     */
    public FileLogReader(String name) throws IOException, KernelLogException {
        this(new File(name));
    }

    /**
       Construct a new FileLogReader.
       @param file The file object to read.
       @throws IOException If the file cannot be read.
       @throws KernelLogException If there is a problem reading the log.
     */
    public FileLogReader(File file) throws IOException, KernelLogException {
        super(new FileInputStream(file));
    }
}