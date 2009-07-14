package kernel.log;

import static rescuecore2.misc.EncodingTools.writeInt32;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;

/**
   A class for writing the kernel log to a file.
 */
public class FileLogWriter extends StreamLogWriter {
    /**
       Create a file log writer and open it for writing. This will inspect the configuration for the "kernel.logname" parameter and attempt to open a file.
       @param config The kernel configuration.
       @throws KernelLogException If the log file cannot be opened.
    */
    public FileLogWriter(Config config) throws KernelLogException {
        super(new BufferedOutputStream(new FileOutputStream(new File(config.getValue("kernel.logname")))));
    }
}