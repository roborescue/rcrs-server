package rescuecore2.standard.kernel;

/**
   An exception class for map validation errors.
 */
public class MapValidationException extends Exception {
    /**
       Construct a MapValidationException with no error message.
     */
    public MapValidationException() {
        super();
    }

    /**
       Construct a MapValidationException with an error message.
       @param msg The error message.
     */
    public MapValidationException(String msg) {
        super(msg);
    }

    /**
       Construct a MapValidationException with an underlying cause.
       @param cause The underlying cause.
     */
    public MapValidationException(Throwable cause) {
        super(cause);
    }

    /**
       Construct a MapValidationException with an error message and an underlying cause.
       @param msg The error message.
       @param cause The underlying cause.
     */
    public MapValidationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
