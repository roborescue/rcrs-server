package maps;

/**
   Exceptions related to maps.
*/
public class MapException extends Exception {
    /**
       Construct a MapException with no error message.
    */
    public MapException() {
        super();
    }

    /**
       Construct a MapException with an error message.
       @param msg The error message.
    */
    public MapException(String msg) {
        super(msg);
    }

    /**
       Construct a MapException with an underlying cause.
       @param cause The cause.
    */
    public MapException(Throwable cause) {
        super(cause);
    }

    /**
       Construct a MapException with an error message and underlying cause.
       @param msg The error message.
       @param cause The cause.
    */
    public MapException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
