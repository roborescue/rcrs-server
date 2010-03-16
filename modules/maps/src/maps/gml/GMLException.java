package maps.gml;

/**
   Exceptions related to GML.
*/
public class GMLException extends Exception {
    /**
       Construct a GMLException with no error message.
    */
    public GMLException() {
        super();
    }

    /**
       Construct a GMLException with an error message.
       @param msg The error message.
    */
    public GMLException(String msg) {
        super(msg);
    }

    /**
       Construct a GMLException with an underlying cause.
       @param cause The cause.
    */
    public GMLException(Throwable cause) {
        super(cause);
    }

    /**
       Construct a GMLException with an error message and underlying cause.
       @param msg The error message.
       @param cause The cause.
    */
    public GMLException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
