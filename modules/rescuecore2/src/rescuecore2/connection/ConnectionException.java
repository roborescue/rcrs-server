package rescuecore2.connection;

/**
   An error with a Connection. This usually represents an illegal state such as sending a message after shutdown.
 */
public class ConnectionException extends Exception {
    /**
       Construct a ConnectionException with no explanation.
     */
    public ConnectionException() {
        super();
    }

    /**
       Construct a ConnectionException with an error message.
       @param msg The error message.
     */
    public ConnectionException(String msg) {
        super(msg);
    }

    /**
       Construct a ConnectionException that has an underlying cause.
       @param cause The cause of this exception.
     */
    public ConnectionException(Throwable cause) {
        super(cause);
    }

    /**
       Construct a ConnectionException with an error message and an underlying cause.
       @param msg The error message.
       @param cause The cause of this exception.
     */
    public ConnectionException(String msg,  Throwable cause) {
        super(msg, cause);
    }
}
