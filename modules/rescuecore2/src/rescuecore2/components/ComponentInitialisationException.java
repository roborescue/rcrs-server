package rescuecore2.components;

/**
   Exception class for problems with initialisation of components.
 */
public class ComponentInitialisationException extends Exception {
    /**
       Construct an exception with no useful information.
     */
    public ComponentInitialisationException() {
    }

    /**
       Construct an exception with an error message.
       @param msg A message describing the problem.
     */
    public ComponentInitialisationException(final String msg) {
        super(msg);
    }

    /**
       Construct an exception with an underlying cause.
       @param cause The underlying cause of this exception.
     */
    public ComponentInitialisationException(final Throwable cause) {
        super(cause);
    }

    /**
       Construct an exception with an error message and underlying cause.
       @param msg A message describing the problem.
       @param cause The underlying cause of this exception.
     */
    public ComponentInitialisationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
