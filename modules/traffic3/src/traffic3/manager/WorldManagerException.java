package traffic3.manager;

/**
 * This exception will be thrown when somve object whome id already exists in the WorldMaanger added.
 */
public class WorldManagerException extends Exception {

    /**
     * Constructor.
     * @param message message
     */
    public WorldManagerException(String message) {
        super(message);
    }
}