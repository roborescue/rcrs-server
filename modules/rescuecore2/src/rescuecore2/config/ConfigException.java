package rescuecore2.config;

/**
   Exception class for problems with config files.
 */
public class ConfigException extends Exception {
    /**
       Construct an exception with just a filename, no message or underlying cause.
       @param filename The name of the config file that caused the problem.
     */
    public ConfigException(final String filename) {
        this(filename, -1, "Unknown error", null);
    }

    /**
       Construct an exception with a filename and error message.
       @param filename The name of the config file that caused the problem.
       @param msg A message describing the problem.
     */
    public ConfigException(final String filename, final String msg) {
        this(filename, -1, msg, null);
    }

    /**
       Construct an exception with a filename and an underlying cause.
       @param filename The name of the config file that caused the problem.
       @param cause The underlying cause of this exception.
     */
    public ConfigException(final String filename, final Throwable cause) {
        this(filename, -1, cause.toString(), cause);
    }

    /**
       Construct an exception with a filename and a line number.
       @param filename The name of the config file that caused the problem.
       @param linenumber The line number where the problem occurred.
     */
    public ConfigException(final String filename, final int linenumber) {
        this(filename, linenumber, "Unknown error", null);
    }

    /**
       Construct an exception with a filename, line number and error message.
       @param filename The name of the config file that caused the problem.
       @param linenumber The line number where the problem occurred.
       @param msg A message describing the problem.
     */
    public ConfigException(final String filename, final int linenumber, final String msg) {
        this(filename, linenumber, msg, null);
    }

    /**
       Construct an exception with a filename, line number and underlying cause.
       @param filename The name of the config file that caused the problem.
       @param linenumber The line number where the problem occurred.
       @param cause The underlying cause of this exception.
     */
    public ConfigException(final String filename, final int linenumber, final Throwable cause) {
        this(filename, linenumber, cause.toString(), cause);
    }

    /**
       Construct an exception with a filename, error message and underlying cause.
       @param filename The name of the config file that caused the problem.
       @param msg A message describing the problem.
       @param cause The underlying cause of this exception.
     */
    public ConfigException(final String filename, final String msg, final Throwable cause) {
        this(filename, -1, msg, cause);
    }

    /**
       Construct an exception with a filename, error message and underlying cause.
       @param filename The name of the config file that caused the problem.
       @param lineNumber The line number where the problem occurred.
       @param msg A message describing the problem.
       @param cause The underlying cause of this exception.
     */
    public ConfigException(final String filename, final int lineNumber, final String msg, final Throwable cause) {
        super((filename == null ? "" : (filename + ": ")) + (lineNumber > 0 ? ("Line " + lineNumber + ": ") : "") + (msg == null ? "" : msg), cause);
    }
}
