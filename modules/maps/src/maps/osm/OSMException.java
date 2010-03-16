package maps.osm;

/**
   Exceptions related to OpenStreetMap.
*/
public class OSMException extends Exception {
    /**
       Construct an OSMException with no error message.
    */
    public OSMException() {
        super();
    }

    /**
       Construct an OSMException with an error message.
       @param msg The error message.
    */
    public OSMException(String msg) {
        super(msg);
    }

    /**
       Construct an OSMException with an underlying cause.
       @param cause The cause.
    */
    public OSMException(Throwable cause) {
        super(cause);
    }

    /**
       Construct an OSMException with an error message and underlying cause.
       @param msg The error message.
       @param cause The cause.
    */
    public OSMException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
