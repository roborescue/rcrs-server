package traffic3;


/**
 *
 */
public class LaunchException extends Exception {

    /**
     * Constructor.
     * @param m message
     */
    public LaunchException(String m) {
        super(m);
    }

    /**
     * Constructor.
     * @param m message
     * @param c cause
     */
    public LaunchException(String m, Throwable c) {
        super(m, c);
    }

    /**
     * Constructor.
     * @param c cause
     */
    public LaunchException(Throwable c) {
        super(c);
    }
}
