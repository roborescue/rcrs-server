package rescuecore2.components;

/**
   Exception class for component connection errors.
 */
public class ComponentConnectionException extends Exception {
    /**
       Construct an exception with an error message.
       @param msg A message describing the problem.
     */
    public ComponentConnectionException(final String msg) {
        super(msg);
    }

    /**
       Construct an exception with an underlying cause.
       @param cause The underlying cause of this exception.
     */
    public ComponentConnectionException(final Throwable cause) {
        super(cause);
    }

    /**
       Construct an exception with an error message and underlying cause.
       @param msg A message describing the problem.
       @param cause The underlying cause of this exception.
     */
    public ComponentConnectionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
