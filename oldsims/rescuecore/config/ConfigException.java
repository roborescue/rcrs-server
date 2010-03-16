package rescuecore.config;

/**
   Exception class for problems with config files.
 */
public class ConfigException extends Exception {
    /**
       Construct an exception with just a filename, no message or underlying cause.
       @param filename The name of the config file that caused the problem.
     */
    public ConfigException(final String filename) {
        super(filename + ": unknown error");
    }

    /**
       Construct an exception with a filename and error message.
       @param filename The name of the config file that caused the problem.
       @param msg A message describing the problem.
     */
    public ConfigException(final String filename, final String msg) {
        super(filename + ": " + msg);
    }

    /**
       Construct an exception with a filename and an underlying cause.
       @param filename The name of the config file that caused the problem.
       @param cause The underlying cause of this exception.
     */
    public ConfigException(final String filename, final Throwable cause) {
        super(filename + ": " + cause.toString(), cause);
    }

    /**
       Construct an exception with a filename, error message and underlying cause.
       @param filename The name of the config file that caused the problem.
       @param msg A message describing the problem.
       @param cause The underlying cause of this exception.
     */
    public ConfigException(final String filename, final String msg, final Throwable cause) {
        super(filename + ": " + msg, cause);
    }
}
