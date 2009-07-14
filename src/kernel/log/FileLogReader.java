package kernel.log;

import java.io.File;
import java.io.FileInputStream;

/**
   A log reader that reads from a file.
 */
public class FileLogReader extends StreamLogReader {
    /**
       Construct a new FileLogReader.
       @param name The name of the file to read.
     */
    public FileLogReader(String name) {
        this(new File(name));
    }

    /**
       Construct a new FileLogReader.
       @param file The file object to read.
     */
    public FileLogReader(File file) {
        super(new FileInputStream(file));
    }
}