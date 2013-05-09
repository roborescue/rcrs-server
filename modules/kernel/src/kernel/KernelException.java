package kernel;

/**
   Root of the kernel exception heirarchy.
 */

public class KernelException extends Exception {
    /**
       Construct a kernel exception with no information.
     */
    public KernelException() {
        super();
    }

    /**
       Construct a kernel exception with an error message.
       @param msg The error message.
     */
    public KernelException(String msg) {
        super(msg);
    }

    /**
       Construct a kernel exception that was caused by another exception.
       @param cause The cause of this exception.
     */
    public KernelException(Throwable cause) {
        super(cause);
    }

    /**
       Construct a kernel exception with an error message and an underlying cause.
       @param msg The error message.
       @param cause The cause of this exception.
     */
    public KernelException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
