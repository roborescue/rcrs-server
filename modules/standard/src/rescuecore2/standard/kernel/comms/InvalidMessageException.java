package rescuecore2.standard.kernel.comms;

/**
   An exception indicating that an agent communication message was invalid.
 */
public class InvalidMessageException extends Exception {
    /**
       Create an InvalidMessageException with a particular error message.
       @param msg The error message.
    */
    public InvalidMessageException(String msg) {
        super(msg);
    }
}
