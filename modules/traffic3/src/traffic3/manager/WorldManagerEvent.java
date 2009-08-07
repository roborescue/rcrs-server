package traffic3.manager;

import traffic3.objects.TrafficObject;

/**
 *
 */
public class WorldManagerEvent {

    private Object eventSource;
    private TrafficObject[] changedObjects;

    /**
     * Constructor.
     * @param s event source
     */
    public WorldManagerEvent(Object s) {
        this(s, null);
    }

    /**
     * Constructor.
     * @param es event source
     * @param co changed objects
     */
    public WorldManagerEvent(Object es, TrafficObject[] co) {
        eventSource = es;
        changedObjects = co;
    }

    /**
     * get event source.
     * @return source
     */
    public Object getSource() {
        return eventSource;
    }

    /**
     * get changed objects.
     * @return changed objects
     */
    public TrafficObject[] getChengedObjectList() {
        return changedObjects;
    }
}
