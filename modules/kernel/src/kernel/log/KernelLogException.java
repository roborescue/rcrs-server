package kernel.log;

import kernel.KernelException;

/**
   Kernel logging exceptions.
 */
public class KernelLogException extends KernelException {
    /**
       Construct a kernel log exception with no information.
     */
    public KernelLogException() {
        super();
    }

    /**
       Construct a kernel log exception with an error message.
       @param msg The error message.
     */
    public KernelLogException(String msg) {
        super(msg);
    }

    /**
       Construct a kernel log exception that was caused by another exception.
       @param cause The cause of this exception.
     */
    public KernelLogException(Throwable cause) {
        super(cause);
    }

    /**
       Construct a kernel log exception with an error message and an underlying cause.
       @param msg The error message.
       @param cause The cause of this exception.
     */
    public KernelLogException(String msg, Throwable cause) {
        super(msg, cause);
    }
}