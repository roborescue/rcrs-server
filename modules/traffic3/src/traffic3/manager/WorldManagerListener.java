package traffic3.manager;

/**
 *
 */
public interface WorldManagerListener {

    /**
     * added.
     * @param e event
     */
    void added(WorldManagerEvent e);

    /**
     * removed.
     * @param e event
     */
    void removed(WorldManagerEvent e);

    /**
     * changed.
     * @param e event
     */
    void changed(WorldManagerEvent e);

    /**
     * map updated.
     * @param e event
     */
    void mapUpdated(WorldManagerEvent e);

    /**
     * agent updated.
     * @param e event
     */
    void agentUpdated(WorldManagerEvent e);

    /**
     * inputted.
     * @param e event
     */
    void inputted(WorldManagerEvent e);
}
