package rescuecore2.misc;

import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.Constants;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;

/**
   A utility class for processing command line options.
 */
public final class CommandLineOptions {
    /** The command-line flag for specifying a config file. */
    public static final String CONFIG_FLAG = "-c";

    /** The command-line flag for specifying the kernel host. */
    public static final String HOST_FLAG = "-h";

    /** The command-line flag for specifying the kernel port. */
    public static final String PORT_FLAG = "-p";

    private CommandLineOptions() {}

    /**
       Process a set of command line arguments. Config files ("-c" options) will be read and individual config entries ("--x=y" options) will be processed. Unrecognised arguments will be returned in order.
       @param args The command line options.
       @param config A Config to populate.
       @return All unprocessed options. This will not be null.
       @throws IOException If there is a problem reading a config file.
       @throws ConfigException If there is a problem processing a config file.
     */
    public static String[] processArgs(String[] args, Config config) throws IOException, ConfigException {
        List<String> all = Arrays.asList(args);
        List<String> result = new ArrayList<String>();
        Iterator<String> it = all.iterator();
        while (it.hasNext()) {
            String next = it.next();
            if (CONFIG_FLAG.equals(next)) {
                config.read(new File(it.next()));
            }
            else if (HOST_FLAG.equals(next)) {
                config.setValue(Constants.KERNEL_HOST_NAME_KEY, it.next());
            }
            else if (PORT_FLAG.equals(next)) {
                config.setValue(Constants.KERNEL_PORT_NUMBER_KEY, it.next());
            }
            else if (next.startsWith("--") && next.indexOf("=") != -1) {
                int index = next.indexOf("=");
                String key = next.substring(2, index);
                String value = next.substring(index + 1);
                config.setValue(key, value);
            }
            else {
                result.add(next);
            }
        }
        return result.toArray(new String[0]);
    }
}
