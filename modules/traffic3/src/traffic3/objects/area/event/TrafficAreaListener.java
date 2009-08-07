package traffic3.objects.area.event;

/**
 *
 */
public interface TrafficAreaListener {

    /**
     * This method will be called when any agent enter the area.
     * @param event event
     */
    void entered(TrafficAreaEvent event);

    /**
     * This method will be called when any agent exit the area.
     * @param event event
     */
    void exited(TrafficAreaEvent event);
}
