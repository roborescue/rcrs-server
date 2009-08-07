package traffic3.log.event;

/**
 *
 */
public interface LogListener {

    /**
     * @param event event
     */
    void log(LogEvent event);
}
