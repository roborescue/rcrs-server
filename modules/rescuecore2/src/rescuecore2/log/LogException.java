package rescuecore2.log;

/**
   Logging exceptions.
 */
public class LogException extends Exception {
    /**
       Construct a log exception with no information.
     */
    public LogException() {
        super();
    }

    /**
       Construct a log exception with an error message.
       @param msg The error message.
     */
    public LogException(String msg) {
        super(msg);
    }

    /**
       Construct a log exception that was caused by another exception.
       @param cause The cause of this exception.
     */
    public LogException(Throwable cause) {
        super(cause);
    }

    /**
       Construct a log exception with an error message and an underlying cause.
       @param msg The error message.
       @param cause The cause of this exception.
     */
    public LogException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
